
read_term (Term) :-
        current_input (S), (\), (=:=), readTerm (S, Term, "Hello").

:- 3*4-foo(3, 4, hat, [3, 4, 5 | tail]).

foo(3, (atom), (Adam), bar (3, 4, 5)).

%- .

--> .

foo.

-243.

%foo (A, B) :- 
%        foo (A+1, B-1).
%foo (A, B) :- 
%        A = 32, B=0.

%bar (X) :- 
%        X = 3.

%baz (X) :- 
%        X = [a, "Shawn", {c} | D], 
%        D=X-2.

%foo (A, B) :- 
%        A=X-2.

%:- write (hello).
%:- nl.

