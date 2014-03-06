%
% system.pl
% 1999 Shawn Vincent
%
% Definitions of some system predicates
%


%
% set_input/1 is similar to current_input/1, except that the
% parameter must be a ground term.
%
set_input (S_or_a) :- var (S_or_a), throw (instantiation_error).  
set_input (S_or_a) :- current_input (S_or_a).

%
% set_output/1 is similar to current_output/1, except that the
% parameter must be a ground term.
%
set_output (S_or_a) :- var (S_or_a), throw (instantiation_error).
set_output (S_or_a) :- current_output (S_or_a).

%
% open/3 opens a stream with default options.
%
open(Source_sink, Mode, Stream) :- open (Source_sink, Mode, Stream, []).

% native open/4
% FIX TO BE:
%open(Source_sink, Mode, Stream, Options) :-
%        java:'com.svincent.moksa.Io$Open_4' (Source_sink, Mode,
%                                                Stream, Options);

% native close/2 

%
% stream_property/2 returns true if the given stream has the
% given property.  Or, it can be used to loop through all
% the properties a stream has.
% XXX untested
%
stream_property (Stream, Property) :-
        get_stream_properties (Stream, Properties),
        member_of (Property, Properties).



%
% flush_output/0 flushes the current output stream.
%
flush_output :- current_output (S), flush_output (S).

% native flush_output/1



%
% XXX stream_property/2
%

%
% XXX at_end_of_stream/0
%

%
% XXX set_stream_position/2
%

%
% get_char/1 gets a character from the current input stream.
%
get_char (Char) :- current_input (S), get_char (S, Char).

%
% put_char/1 puts a character to the current output stream.
%
put_char (Char) :- current_output (S), put_char (S, Char).

%
% nl/0 prints a newline character to current output stream
%
nl :- current_output (S), nl (S).


%
% XXX Character code I/O
%

%
% XXX Term I/O
%

write (Term) :- current_output (S), write (S, Term).

%
% XXX Operator stuff
%

%
% XXX Character conversion stuff.
%

%
%
%

% append/3
append ([], L, L).
append ([A|L1], L2, [A|L3]) :- append (L1, L2, L3).

% member_of/2.
member_of (X, [X|_]).
member_of (X, [_|L]) :- member_of (X, L).


