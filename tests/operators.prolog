% --- Operators

% Operands

% Operators as Functors

% fx
parse_prolog_lterm ([name(Op)|ATokens], Priority, LTerm, Rest) :-
        write ('Trying fx '), write (Tokens), nl,
        is (SubPriority, Priority-1),
        current_op (OpPriority, fx, Op),
        parse_prolog_term (ATokens, SubPriority, A, Rest),
        >= (Priority, OpPriority),
        =.. (LTerm, [Op, A]).

% fy
parse_prolog_lterm ([name(Op)|ATokens], Priority, LTerm, Rest) :-
        write ('Trying fy '), write (Tokens), nl,
        current_op (OpPriority, fy, Op),
        parse_prolog_term (Tokens, Priority, A, Rest),
        >= (Priority, OpPriority),
        =.. (LTerm, [Op, A]).

% xfx
parse_prolog_lterm (Tokens, Priority, LTerm, Rest) :-
        write ('Trying xfx '), write (Tokens), nl,
        is (SubPriority, Priority-1),
        parse_prolog_term (Tokens, SubPriority, A, [name(Op)|BTokens]),
        write ('Got op '), write (op (Op)), nl,
        current_op (OpPriority, xfx, Op),
        write ('Got op '), write (op (Op, OpPriority)), nl,
        >= (Priority, OpPriority),
        parse_prolog_term (BTokens, SubPriority, B, Rest),
        =.. (LTerm, [Op, A, B]).

% xfy
parse_prolog_lterm (Tokens, Priority, LTerm, Rest) :-
        write ('Trying xfy '), write (Tokens), nl,
        is (SubPriority, Priority-1),
        parse_prolog_term (Tokens, SubPriority, A, [name(Op)|BTokens]),
        current_op (OpPriority, xfy, Op),
        >= (Priority, OpPriority),
        parse_prolog_term (BTokens, Priority, B, Rest),
        =.. (LTerm, [Op, A, B]).

% yfx
parse_prolog_lterm (Tokens, Priority, LTerm, Rest) :-
        write ('Trying yfx '), write (Tokens), nl,
        is (SubPriority, Priority-1),
        parse_prolog_lterm (Tokens, Priority, A, [name(Op)|BTokens]),
        current_op (OpPriority, yfx, Op),
        >= (Priority, OpPriority),
        parse_prolog_term (BTokens, SubPriority, B, Rest),
        =.. (LTerm, [Op, A, B]).

% xf
parse_prolog_lterm (Tokens, Priority, LTerm, Rest) :-
        write ('Trying xf '), write (Tokens), nl,
        is (SubPriority, Priority-1),
        parse_prolog_term (Tokens, SubPriority, A, [name(Op)|Rest]),
        current_op (OpPriority, xf, Op),
        >= (Priority, OpPriority),
        =.. (LTerm, [Op, A]).

% yf
parse_prolog_lterm (Tokens, Priority, LTerm, Rest) :-
        write ('Trying yf '), write (Tokens), nl,
        parse_prolog_lterm (Tokens, Priority, A, [name(Op)|Rest]),
        current_op (OpPriority, yf, Op),
        >= (Priority, OpPriority),
        =.. (LTerm, [Op, A]).

% An operand is a term.
parse_prolog_term (Tokens, Priority, Term, Rest) :-
        write ('Trying lterm term...'), %write (Tokens),
        write (', '),
        write (priority (Priority)), nl,
        parse_prolog_lterm (Tokens, Priority, Term, Rest),
        write ('Got lterm term'), write (Name), nl.

parse_prolog_lterm (Tokens, Priority, LTerm, Rest) :-
        is (NewPriority, Priority-1),
        write ('Trying term lterm...'), %write (Tokens),
        write (priority (NewPriority)), nl,
        parse_prolog_term (Tokens, NewPriority, LTerm, Rest),
        write ('Got term lterm'), write (Name), nl.

%parse_prolog_term (Tokens, Priority, Term) :- write (Tokens), nl.
