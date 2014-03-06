%%
%% parser.prolog
%% 1999 Shawn Vincent
%%
%% A parser for the Prolog language, written in Prolog.
%%


read_term (Term) :-
        current_input (S), readTerm (S, Term).
        
read_term (Stream, Term) :-
        tokenize_prolog_term (Stream, Tokens, _),
        %write ('Got tokens '), write (Tokens), nl,
        parse_prolog_term (Tokens, Term).
        %write ('Parsed term '), write (Term), nl.

%% -------------------------------------------------------------------------
%% ---- Tokenize a stream containing a Prolog term -------------------------
%% -------------------------------------------------------------------------

%% 'Vars' is a list of name->Variable mappings, in the form
%%
%%         varDef (name, Variable).
%%
%% They are not returned: just maintained along the list to uniquify variables.
%%

tokenize_prolog_term (S, Tokens, Vars) :-
        get_prolog_token (S, Next),
        prolog_term_to_tokens (Next, S, Tokens, []).

prolog_term_to_tokens (end, S, [end], Vars).
prolog_term_to_tokens (end_of_file, S, [], Vars).

prolog_term_to_tokens (variable (Name), S, Tokens, Vars) :-
        make_unique_variable_token (Name, Vars, VarToken, NewVars),
        = (Tokens, [VarToken|Rest]),
        get_prolog_token (S, NextToken),
        prolog_term_to_tokens (NextToken, S, Rest, NewVars).

prolog_term_to_tokens (T, S, [T|Rest], Vars) :-
        get_prolog_token (S, NextToken),
        prolog_term_to_tokens (NextToken, S, Rest, Vars).


%%
%% make_unique_variable_token/4 constructs a new variable def list
%% that is guaranteed to have a variable with the given name in it.
%%

% didn't find it: create a new one.
make_unique_variable_token (Name, [], VarToken, NewVars) :-
        
        % XXX this is INCREDIBLY CREEPY
        set_var_nameXXX (Name, Var),
        
        %write ('Make var '), write (Var), nl,
        =(VarDef, varDef(Name, Var)),
        =(VarToken, variable (Name, Var)),
        =(NewVars, [VarDef]).

% found it.  Just return it (copy the rest of our list into NewVars, too)
make_unique_variable_token (Name, [VarDef|MoreVars], VarToken,
                               [VarDef|MoreVars]) :-
        =(VarDef, varDef(Name, Var)),
        =(VarToken, variable (Name, Var)).

% recursive step.  Look further
make_unique_variable_token (Name, [X|MoreVars], VarToken, [X|NewVars]) :-
        make_unique_variable_token (Name, MoreVars, VarToken, NewVars).

%% -------------------------------------------------------------------------
%% ---- Parse a tokenized Prolog term --------------------------------------
%% -------------------------------------------------------------------------


parse_prolog_term ([end_of_file|Rest], end_of_file).

parse_prolog_term (Tokens, Term) :-
        parse_prolog_term (Tokens, 1201, Term, [end]).


%%
%% parse_prolog_term/4
%%   parse_prolog_term (InTokens, MaxPriority, Term, Leftovers)
%%

%% --- EOF

%% --- Numbers

parse_prolog_term (Tokens, Priority, Term, Rest) :-
        %write ('Trying integer, tokens == '), write (Tokens),
        %write (', want rest == '), write (Rest), nl,
        = (Tokens, [integer(Number)|Rest]),
        %write ('Got integer '), write (Number), nl,
        >= (Priority, 0),
        is (Term, Number).
parse_prolog_term ([float(Number)|Rest], Priority, Term, Rest) :-
        %write ('Got float '), write (Number), nl,
        >= (Priority, 0),
        is (Term, Number).

%% --- Negative Numbers

parse_prolog_term ([name ('-'), integer(Number)|Rest], Priority, Term, Rest) :-
        %%write ('Got integer '), write (Number), nl,
        >= (Priority, 0),
        is (Term, -(Number)).
parse_prolog_term ([name ('-'), float(Number)|Rest], Priority, Term, Rest):-
        %write ('Got float '), write (Number), nl,
        >= (Priority, 0),
        is (Term, -(Number)).

%% --- Variables

%% XXX fix vars here...
parse_prolog_term ([variable(Name, Var)|Rest], Priority, Term, Rest) :-
        %write ('Got variable'), write (Name), nl,
        >= (Priority, 0),
        = (Term, Var).

%% --- Compound terms

parse_prolog_term ([name(Name), open|Rest], Priority, Term, Tail) :-
        >= (Priority, 0),
        parse_prolog_arg_list (Rest, Priority, Args, Tail),
        =.. (Term, [Name|Args]).
        %write ('Got compound term '), write (Term), nl.

parse_prolog_arg_list (Tokens, Priority, ArgList, Tail) :-
        %write ('Trying arg list '), write (Tokens), nl,
        parse_prolog_exp (Tokens, Priority, Exp, MoreArgTokens),
        parse_prolog_more_args (MoreArgTokens, Priority, MoreArgs, Tail),
        = (ArgList, [Exp|MoreArgs]).

parse_prolog_more_args ([close|Rest], Priority, [], Rest).
        %write ('Got arg list end: '), write (Rest), nl.
parse_prolog_more_args ([name (',')|Rest], Priority, ArgList, Tail) :-
        %write ('Trying arg list continuation: '), write (Rest), nl,
        parse_prolog_exp (Rest, Priority, Exp, MoreArgTokens),
        %write ('Got arg list element: '), write (Exp),
        %write (more (MoreArgTokens)), write (priority (Priority)),
        %write (tail (Tail)), nl,
        parse_prolog_more_args (MoreArgTokens, Priority, MoreArgs, Tail),
        = (ArgList, [Exp|MoreArgs]).
        %write ('Got arg list continuation.'), write (ArgList), nl.


%% --- Atoms

parse_prolog_term ([name(Name)|Rest], Priority, Term, Rest) :-
        operator (Name),
        %write ('Got operator atom '), write (Name), nl,
        >= (Priority, 1201),
        = (Term, Name).

parse_prolog_term ([name(Name)|Rest], Priority, Term, Rest) :-
        %write ('Got non-operator atom '), write (Name), nl,
        >= (Priority, 0),
        = (Term, Name).

%% --- Compound Terms: character code list notation

parse_prolog_term ([string(S)|Rest], Priority, Term, Rest) :-
        >= (Priority, 0),
        = (Term, S).

%% --- Compound Terms: Curly bracket notation

parse_prolog_term ([open_curly|BodyTokens], Priority, Term, Rest) :-
        >= (Priority, 0),
        %write ('Trying curly term: got rest == '), write (Rest), nl,
        parse_prolog_term (BodyTokens, 1201, Subterm, [close_curly|Rest]),
        %write ('Got curly sub-term'), nl,
        =..(Term, ['{}', Subterm])
        %,write ('Got curly term: '), write (Term), nl
        .

%% --- Parenthetical terms

% a term with small priority can always appear where a term of high
% priority is allowed.

parse_prolog_term ([open|Rest], Priority, Term, Tail) :-
        %write ('*** Trying parenthesized term ***: '), write (Rest), nl,
        % we ignore it's priority, and set the result priority to 0.
        parse_prolog_term (Rest, (1201), Term, [close|Tail]).
        %write ('Got parenthesized term '), write (Term), nl.


%% --- Expressions

parse_prolog_exp ([name(Name)|Rest], Priority, Exp, Rest) :-
        %write ('Got atom expression '), write (Name), nl,
        operator_no_comma (Name),
        = (Exp, Name).

parse_prolog_exp (Tokens, Priority, Exp, Rest) :-
        %write ('Got term expression'), nl,
        parse_prolog_term (Tokens, (999), Term, Rest),
        = (Exp, Term).

%% --- Compound Terms: Operator Notation.

%% prefix terms
parse_prolog_term ([name(Op)|ATokens], Priority, Term, Rest) :-
        %write ('Trying prefix operator.'), nl,
        current_op (OPri, Op_Specifier, Op),
        calculate_prolog_prefix_subpriority (Op_Specifier, OPri, APri),
        >= (Priority, OPri),
        parse_prolog_term (ATokens, APri, A, Rest),
        =.. (Term, [Op, A]).

%% comma infix term.
parse_prolog_term (Tokens, Priority, Term, Rest) :-
        %write ('Trying infix operator.'), nl,
        % split tokens up.
        private_append (ATokens, [name (',')|BTokens], Tokens),
        = (Op, (',')),
        current_op (OPri, Op_Specifier, Op),
        >= (Priority, OPri),
        calculate_prolog_infix_subpriority (Op_Specifier, OPri, APri, BPri),
        %write ('Got infix term '), write (Op), nl,
        parse_prolog_term (ATokens, APri, A, []),
        parse_prolog_term (BTokens, BPri, B, Rest),
        =.. (Term, [Op, A, B]).

%% other infix terms.
parse_prolog_term (Tokens, Priority, Term, Rest) :-
        %write ('Trying infix operator.'), nl,
        % split tokens up.
        private_append (ATokens, [name (Op)|BTokens], Tokens),
        current_op (OPri, Op_Specifier, Op),
        >= (Priority, OPri),
        calculate_prolog_infix_subpriority (Op_Specifier, OPri, APri, BPri),
        %write ('Got infix term '), write (Op), nl,
        parse_prolog_term (ATokens, APri, A, []),
        parse_prolog_term (BTokens, BPri, B, Rest),
        =.. (Term, [Op, A, B]).

%% postfix terms.
%% not tested: there are no standard postfix operators.
parse_prolog_term (Tokens, Priority, Term, Rest) :-
        %write ('Trying postfix operator.'), nl,
        % split tokens up.
        private_append (ATokens, [name (Op)|Rest], Tokens),
        current_op (OPri, Op_Specifier, Op),
        >= (Priority, OPri),
        calculate_prolog_postfix_subpriority (Op_Specifier, OPri, APri),
        parse_prolog_term (ATokens, APri, A, []),
        =.. (Term, [Op, A]).

%% the idea of breaking the input token stream up using append/3
%% was borrowed from jProlog.  It was a fun idea, although it's probably
%% slow.  I need to rewrite this parser sometime.

calculate_prolog_infix_subpriority (xfx, P, AP, BP) :-
        is (AP, -(P, 1)), is (BP, -(P, 1)).
calculate_prolog_infix_subpriority (yfx, P, AP, BP) :-
        is (AP, P), is (BP, -(P, 1)).
calculate_prolog_infix_subpriority (xfy, P, AP, BP) :-
        is (AP, -(P, 1)), is (BP, P).

calculate_prolog_prefix_subpriority (fx, P, AP) :- is (AP, -(P, 1)).
calculate_prolog_prefix_subpriority (fy, P, AP) :- is (AP, P).

calculate_prolog_postfix_subpriority (xf, P, AP) :- is (AP, -(P, 1)).
calculate_prolog_postfix_subpriority (yf, P, AP) :- is (AP, P).

%% --- Compound Terms: List notation

parse_prolog_term ([open_list|ListTokens], Priority, Term, Rest) :-
        >= (Priority, 0),
        %write ('Trying list term: got rest == '), write (Rest), nl,
        parse_prolog_term_list (ListTokens, Priority, List, Rest),
        =(Term, List)
        %,write ('Got list term: '), write (Term), nl
        .

parse_prolog_term_list (Tokens, Priority, List, Tail) :-
        %write ('Trying list '), write (Tokens), nl,
        parse_prolog_exp (Tokens, Priority, Exp, MoreListTokens),
        parse_prolog_more_list (MoreListTokens, Priority, MoreList, Tail),
        = (List, [Exp|MoreList])
        %,write ('Got list: '), write (List), nl
        .

parse_prolog_more_list ([close_list|Rest], Priority, [], Rest)
        %:-write ('Got list end.'), write (Rest), nl
        .
parse_prolog_more_list ([name (',')|Rest], Priority, List, Tail) :-
        %write ('Trying list continuation.'), write (Rest), nl,
        parse_prolog_exp (Rest, Priority, Exp, MoreListTokens),
        parse_prolog_more_list (MoreListTokens, Priority, MoreList, Tail),
        = (List, [Exp|MoreList])
        %,write ('Got list continuation.'), write (List), nl
        .
parse_prolog_more_list ([head_tail_separator|Rest], Priority, List, Tail) :-
        %write ('Trying list tail seperator.'), write (Rest),
        %write (tail (Tail)), nl,
        parse_prolog_exp (Rest, Priority, Exp, [close_list|Tail]),
        = (List, Exp)
        %,write ('Got list tail.'), write (List), nl
        .



        

%% -------------------------------------------------------------------------
%% ---- Prolog operator support --------------------------------------------
%% -------------------------------------------------------------------------

operator_no_comma (',') :- !, fail.
operator_no_comma (PossibleOp) :- operator (PossibleOp).

operator (Operator) :- current_op (_, _, Operator).

current_op (1200, xfx, (':-')).
current_op (1200, xfx, ('-->')).
current_op (1200, fx, (':-')).
current_op (1200, fx, ('?-')).
current_op (1100, xfy, (';')).
current_op (1050, xfy, ('->')).
current_op (1000, xfy, (',')).

current_op (700, xfx, ('=')).
current_op (700, xfx, (\=)).

current_op (700, xfx, ('==')).
current_op (700, xfx, (\==)).
current_op (700, xfx, ('@<')).
current_op (700, xfx, ('@=<')).
current_op (700, xfx, ('@>')).
current_op (700, xfx, ('@>=')).

current_op (700, xfx, ('=..')).

current_op (700, xfx, ('is')).
current_op (700, xfx, ('=:=')).
current_op (700, xfx, (=\=)).
current_op (700, xfx, ('<')).
current_op (700, xfx, ('=<')).
current_op (700, xfx, ('>')).
current_op (700, xfx, ('>=')).

current_op (600, xfy, (':')).

current_op (500, yfx, ('+')).
current_op (500, yfx, ('-')).
current_op (500, yfx, (/\)).
current_op (500, yfx, (\/)).

current_op (400, yfx, ('*')).
current_op (400, yfx, ('/')).
current_op (400, yfx, ('//')).
current_op (400, yfx, ('rem')).
current_op (400, yfx, ('mod')).
current_op (400, yfx, ('<<')).
current_op (400, yfx, ('>>')).

current_op (200, xfx, ('**')).

current_op (200, xfy, ('^')).

current_op (200, fy, ('-')).
current_op (200, fy, (\)).

current_op (100, xfx, ('@')).

current_op (50, xfx, (':')).

%% XXX fix backslash handling at some point.

% private_append/3 -- version of append/3 that makes this guy self-sufficent.
private_append ([], L, L).
private_append ([A|L1], L2, [A|L3]) :- private_append (L1, L2, L3).


%% -------------------------------------------------------------------------
%% ---- Test Code ----------------------------------------------------------
%% -------------------------------------------------------------------------

%:- open ('library/testParser.prolog', read, SystemFile),
%   read_term (SystemFile, Term),
%   write (Term),
%   nl.


