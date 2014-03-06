%
% test.pl
% Test prolog file.
%

p(f(X), h(Y, f(a)), Z) :- x(Z).
x(f(f(a))).

% --- define 'append/3'.
append([],L,L).
append([A|L1],L2,[A|L3]) :- append(L1,L2,L3).

% --- define 'memberOf/2'.
memberOf(X,[X|_]).
memberOf(X,[_|Y]) :- memberOf(X,Y).

% --- define 'while/2'.
while(X) :- cond(X), body(X), fail.
cond(X) :- memberOf (X, [bob, frank, mahir, jacob]).
body(X) :- write (X), write (' ').

% --- define 'even/1'.
even(X) :- X=2, X>0, !, X < 0.
even(X) :- X=10.

% --- define 'testThrow/0'.
testThrow :- catch (level2, X, write (X)), fail.
testThrow :- write ('Clause 2').

level2 :- X is 2, fail.
level2 :- level3.
level2 :- write ('Not executed').

level3 :- X is 2, fail.
level3 :- throw ('Hello from level 3!').

:- write ('MoksaProlog 0.1 Ready.'), nl.

% XXX write some top-level stuff...

:- write ('That''s all folks!'), nl.

:- append ([a, b, c], [d, e, f], L), write ('Did append, got '), write (L), nl.