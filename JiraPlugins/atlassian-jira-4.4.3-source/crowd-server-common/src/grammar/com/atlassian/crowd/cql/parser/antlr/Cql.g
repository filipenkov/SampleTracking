grammar Cql;

options {
    output=AST;
    ASTLabelType=CommonTree;
}

@parser::header {
package com.atlassian.crowd.cql.parser.antlr;
}

@lexer::header {
package com.atlassian.crowd.cql.parser.antlr;
}

@members {
@Override
public void recover(org.antlr.runtime.IntStream input, org.antlr.runtime.RecognitionException re)
{
    throw new IllegalArgumentException("Search restriction is invalid", re);
}
}

/** Parser rules **/
restriction
    :   expression;

expression
    :   orExpression;

orExpression
    :   (expr+=andExpression -> $expr) (OR expr+=andExpression -> ^(OR $expr+))*;
   
andExpression
    :   (expr+=primary -> $expr) (AND expr+=primary -> ^(AND $expr+))*;
    
propertyExpression
    :   termKey comparison_op^ termValue;
    
primary
    : parExpression
    | propertyExpression
    ;
    
termKey
    :   string;
    
termValue
    :   string
    ;

comparison_op
    :   (EQUALS|LT|GT);
    
parExpression
    :   '('! expression ')'!;

string
    : STRING
    | QUOTE_STRING
    | SQUOTE_STRING
    ;

/** Lexer rules **/
EQUALS
    :    '=';
    
LT
    :   '<';
    
GT
    :   '>';
    
OR 
    :   ('O'|'o')('R'|'r');
    
AND 
    :   ('A'|'a')('N'|'n')('D'|'d');
    
BANG    :   '!';

STRING
    : (ESCAPE | ~(BSLASH | WS | STRINGSTOP))+
    ;
    
QUOTE_STRING
    : (QUOTE (ESCAPE | ~(BSLASH | QUOTE | CONTROLCHARS))* QUOTE)
    ;
        
SQUOTE_STRING
    : (SQUOTE (ESCAPE | ~(BSLASH | SQUOTE | CONTROLCHARS))* SQUOTE)
    ;
    
/**
 * Some significant characters that need to be matched.
 */
LPAREN  : '(';
RPAREN  : ')';
COMMA   : ',';
LBRACKET
        : '[';
RBRACKET
        : ']';

MATCHWS : ( WS+ ) {$channel=HIDDEN;} ;

fragment
MINUS   :  '-';

fragment
EXPONENT
        : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
HEX_DIGIT
        : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
        :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
        |   UNICODE_ESC
        |   OCTAL_ESC
        ;

fragment
OCTAL_ESC
        :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
        |   '\\' ('0'..'7') ('0'..'7')
        |   '\\' ('0'..'7')
        ;

fragment
UNICODE_ESC
        :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
        ;
    
/**
 * These are some characters that we do not use now but we want to reserve. We have not reserved MINUS because we
 * really really really don't want to force people into quoting issues keys and dates.
 */
/*fragment RESERVED_CHARS
    : '{' | '}'
    | '*' | '/' | '%' | '+' | '^'
    | '$' | '#' | '@'
    | '?' | '.' | ';'
    ;*/
    
fragment QUOTE      : '"' ;
fragment SQUOTE     : '\'';
fragment BSLASH     : '\\';
fragment NL     : '\r';
fragment CR     : '\n';
fragment SPACE      : ' ';  
fragment AMPER          : '&';
fragment AMPER_AMPER    : '&&';
fragment PIPE           : '|';  
fragment PIPE_PIPE  : '||';

fragment ESCAPE
    :   BSLASH
    (
           't'
        |  'n'
        |  'r' 
        |  QUOTE 
        |  SQUOTE
        |  BSLASH 
        |  SPACE
        |  'u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT
    )
    ;

/**
 * These are the Tokens that should not be included as a part of an unquoted string.
 */
fragment STRINGSTOP
    : CONTROLCHARS  
    | QUOTE | SQUOTE
    | EQUALS
    | '!' //BANG 
    | LT | GT 
    | LPAREN | RPAREN 
    //| LIKE 
    | COMMA 
    | LBRACKET | RBRACKET
    | PIPE
    | AMPER
    //| RESERVED_CHARS
    | NEWLINE;


/*
 * These are control characters minus whitespace. We use the negation of this set as the set of 
 * characters that we allow in a string.
 *
 * NOTE: This list needs to be synchronised with JqlStringSupportImpl.isJqlControlCharacter.
 */
fragment CONTROLCHARS
    :   '\u0000'..'\u0009'  //Exclude '\n' (\u000a)
    |   '\u000b'..'\u000c'  //Exclude '\r' (\u000d)
    |   '\u000e'..'\u001f'
    |   '\u007f'..'\u009f'
    //The following are Unicode non-characters. We don't want to parse them. Importantly, we wish 
    //to ignore U+FFFF since ANTLR evilly uses this internally to represent EOF which can cause very
    //strange behaviour. For example, the Lexer will incorrectly tokenise the POSNUMBER 1234 as a STRING
    //when U+FFFF is not excluded from STRING.
    //
    //http://en.wikipedia.org/wiki/Unicode
    |   '\ufdd0'..'\ufdef'
    |   '\ufffe'..'\uffff' 
    ;

fragment NEWLINE
    :   NL | CR;

fragment HEXDIGIT
    :   DIGIT | ('A'|'a') | ('B'|'b') | ('C'|'c') | ('D'|'d') | ('E'|'e') | ('F'|'f')
    ;
    
fragment DIGIT
    :   '0'..'9'
    ;

fragment WS 
    :   (SPACE|'\t'|NEWLINE)
    ;

