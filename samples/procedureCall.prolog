%
% procedureCall.prolog
%
% How to define (and call) a procedure (known in Prolog as a "rule").
%

myProcedure (Parm) :- write (Parm), nl.

:- myProcedure ('This is the parameter value').