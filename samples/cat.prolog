%
%
% cat.prolog - a simple implementation of 'cat'.
%


cat (In, Out) :- get_char (In, C), copy_chars (In, C, Out).

% somehow make these private or something....
copy_chars (In, end_of_file, Out) :- !, fail.
copy_chars (In, C, Out) :-
        put_char (Out, C), get_char (In, NextC), copy_chars (In, NextC, Out).

