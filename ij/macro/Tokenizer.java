package ij.macro;
import ij.*;
import java.io.*;

/** This class converts an imageJ macro file file into a token stream. */
public class Tokenizer implements MacroConstants {

    private StreamTokenizer st;
    private int token;
    private String tokenString;
    private double tokenValue;
    private Program pgm;
    private boolean hasUserFunctions;


    /** Uses a StreamTokenizer to convert an ImageJ macro file into a token stream. */
    public Program tokenize(String program) {
        //IJ.showStatus("tokenizing");
        st = new StreamTokenizer(new StringReader(program));
        st.eolIsSignificant(true);
        st.ordinaryChar('-');
        st.ordinaryChar('/');
        st.ordinaryChar('.');
        st.wordChars('_', '_');
        st.slashStarComments(true);
        st.slashSlashComments(true);
        pgm = new Program();
        do {
            getToken();
            addToken();
        } while (token!=EOF);
        if (hasUserFunctions)
        	addUserFunctions();
        if (IJ.debugMode) {
            pgm.dumpSymbolTable();
            pgm.dumpProgram();
        }
        return pgm;
    }

    final void getToken() {
        try {
            token = st.nextToken();
            String ret=null;
            int nextToken;
            switch (st.ttype) {
            case StreamTokenizer.TT_EOF:
                ret = "EOF";
                token = EOF;
                break;
            case StreamTokenizer.TT_EOL:
                ret = "EOL";
                token = EOL;
                break;
            case StreamTokenizer.TT_WORD:
                ret = st.sval;
                token = WORD;
                break;
            case StreamTokenizer.TT_NUMBER:
                ret = ""+st.nval;
                tokenValue = st.nval;
                if (tokenValue==0.0)
                    tokenValue = getHexConstant();
                token = NUMBER;
                break;
            case '"':
            case '\'':
                ret = ""+st.sval;
                token = STRING_CONSTANT;
                break;
            case '+':
                nextToken = st.nextToken();
                if (nextToken=='+')
                    token = PLUS_PLUS;
                else if (nextToken=='=')
                	token = PLUS_EQUAL;
                else
                    st.pushBack();
                break;
            case '-':
                nextToken = st.nextToken();
                if (nextToken=='-')
                    token = MINUS_MINUS;
                else if (nextToken=='=')
                	token = MINUS_EQUAL;
                else
                    st.pushBack();
                break;
            case '*':
                nextToken = st.nextToken();
                if (nextToken=='=')
                    token = MUL_EQUAL;
                else
                    st.pushBack();
                break;
            case '/':
                nextToken = st.nextToken();
                if (nextToken=='=')
                    token = DIV_EQUAL;
                else
                    st.pushBack();
                break;
            case '=':
                nextToken = st.nextToken();
                if (nextToken=='=')
                    token = EQ;
                else
                    st.pushBack();
                break;
            case '!':
                nextToken = st.nextToken();
                if (nextToken=='=')
                    token = NEQ;
                else
                    st.pushBack();
                break;
            case '>':
                nextToken = st.nextToken();
                if (nextToken=='=')
                    token = GTE;
                else if (nextToken=='>')
                    token = SHIFT_RIGHT;
                else {
                    st.pushBack();
                    token = GT;
                }
                break;
            case '<':
                nextToken = st.nextToken();
                if (nextToken=='=')
                    token = LTE;
                else if (nextToken=='<')
                    token = SHIFT_LEFT;
                else {
                    st.pushBack();
                    token = LT;
                }
                break;
            case '&':
                nextToken = st.nextToken();
                if (nextToken=='&')
                    token = LOGICAL_AND;
                else
                    st.pushBack();
                break;
            case '|':
                nextToken = st.nextToken();
                if (nextToken=='|')
                    token = LOGICAL_OR;
                else
                    st.pushBack();
                break;
            default:
                //char s[] = new char[1];
                //s[0] = (char)token;
                //ret = new String(s);
            }
            tokenString = ret;
        } catch (Exception e) {
            return;
        }
        // IJ.log("	  "+ret);
    }

    final void addToken() {
        int tok = token;
        switch (token) {
        case WORD:
            Symbol symbol = pgm.lookupWord(tokenString);
            if (symbol!=null) {
                int type = symbol.getFunctionType();
                if (type==0) {
                    tok = symbol.type;
                    if (tok==FUNCTION)
                    	hasUserFunctions = true;
                    if (tok==VAR)
                    	pgm.hasVars = true;
                } else
                    tok = type;
                tok += pgm.symTabLoc<<16;
            } else {
                pgm.addSymbol(new Symbol(token, tokenString));
                tok += pgm.stLoc<<16;
            }
            break;
        case STRING_CONSTANT:
            pgm.addSymbol(new Symbol(token, tokenString));
            tok += pgm.stLoc<<16;
            break;
        case NUMBER:
            pgm.addSymbol(new Symbol(tokenValue));
            tok += pgm.stLoc<<16;
            break;
        default:
            break;
        }
        pgm.addToken(tok);
    }

    double getHexConstant() {
        try {
            token = st.nextToken();
        } catch (Exception e) {
            return 0.0;
        }
        if (st.ttype != StreamTokenizer.TT_WORD) {
            st.pushBack();
            return 0.0;
        }
        if (!st.sval.startsWith("x")) {
            st.pushBack();
            return 0.0;
        }
        String s = st.sval.substring(1, st.sval.length());
        double n = 0.0;
        try {
            n = Integer.parseInt(s, 16);
        } catch (NumberFormatException e) {
            st.pushBack();
            n = 0.0;
        }
        return n;
    }

	/** Adds user-defined functions to the symbol table. */
	void addUserFunctions() {
		int[] code = pgm.getCode();
		Symbol[] symbolTable = pgm.getSymbolTable();
		int nextToken, address;
		for (int i=0; i<code.length; i++) {
			token = code[i]&0xffff;
			if (token==FUNCTION) {
				nextToken = code[i+1]&0xffff;
				if (nextToken==WORD) {
					address = code[i+1]>>16;
					Symbol sym = symbolTable[address];
					sym.type = USER_FUNCTION;
					sym.value = i+1;  //address of function
					for (int j=0; j<code.length; j++) {
						token = code[j]&0xffff;
						if (token==WORD && (code[j]>>16)==address && j>0 && (code[j-1]&0xfff)!=FUNCTION) {
							code[j] = USER_FUNCTION;
							code[j] += address<<16;
							//IJ.log((code[j]&0xffff)+" "+(code[j]>>16)+" "+USER_FUNCTION+" "+address);
						} else if (token==EOF)
							break;
					}
					// IJ.log(pgm.decodeToken(nextToken, address));
				}					
			} else if (token==EOF)
				break;
		}
	}

} // class Tokenizer