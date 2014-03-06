
foo (A, B) :- 
        foo (A+1, B-1).

foo (A, B) :- 
        A = 32, B=0.

bar (X) :- 
        X = 3.

baz (L) :- 
        X = [a, b, c | D], 
        L = "\x0053\\x0068\\x0061\\x0077\\x006e\ ",
        L = [Head | Tail], % hi
        D=X-2. % hi

baz (X) :- 
        X = "Shawn".

'baz' (X) :- 
        X = [a, b, {c} | D], 
        D=X-2.

foo (A, B) :- 
        A=X-2.

creepyBeast ('\"\t"\''). 

:- write ('hello').
:- nl.

