%
% list.prolog
%
% Some list manipulation.
%

printList (List) :- write ('['), printListTail (List), nl.

printListTail ([]) :-
        write (']'), nl.
printListTail ([Head|Tail]) :-
        write (Head), write ((',')), printListTail (Tail).
printListTail (NonList) :-
        write ('|'), write (NonList), write (']'), nl.

:- %set_prolog_flag (trace, true), set_prolog_flag (debugUnify, true),
        printList ([1, 2, 3, 4, 5, 6, 7, 8, 9, 10]).
        %set_prolog_flag (trace, false).
