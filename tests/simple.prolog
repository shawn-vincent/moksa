%
%
% simple.prolog
%
%


append_test ([], L, L).
append_test ([A|L1], L2, [A|L3]) :- append_test (L1, L2, L3).

:- append_test ([1, 2], [3, 4], L), current_output (S), write (S, L), nl.
