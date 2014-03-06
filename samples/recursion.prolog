%
% recursion.prolog
%
% Simple recursion in Prolog
%

% this rule succeeds when N=0, so the next rule doesn't run in that case.
recursiveRule (0) :- write ('Done'), nl.

% this rule succeeds for all N>0.
recursiveRule (N) :-
        % --- print the current value of N.
        write ('In recursive rule, N == '), write (N), nl,
        % --- Calculate the next value of N.
        NextN is N - 1,
        % --- do the recursive step.
        recursiveRule (NextN).

:- recursiveRule (10).
