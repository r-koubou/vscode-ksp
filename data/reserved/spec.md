# What's this and Purposes?

Export NI's reserved symbol in tab separated column file.(Plain text)

These files puts on to **data/reserved**.

-> It is for eliminating hard coding to re-build parser program.

# Tab separated column format

## Variables

\<type\>\t\<name\>

## Callback

1. has argument(s)

    \<name\>\t\<type\>(\t\<type\>)*

2. don't have argument

    \<name\>

## Command

1. have argument(s)

    \<return-type\>\t\<name\>\t\<type\>(\t\<type\>)*

2. don't have argument

    \<return-type\>\t\<name\>

## Type

- I   : int
- I[] : int array
- R   : real
- R[] : real array
- S   : string
- S[] : string array
- V   : void ( command not return value )
- PP  : Preprocessor Symbol
