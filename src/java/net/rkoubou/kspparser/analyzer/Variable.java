/* =========================================================================

    Variable.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.Arrays;
import java.util.regex.Pattern;

import net.rkoubou.kspparser.javacc.generated.ASTVariableDeclaration;

/**
 * KSPの値、変数の中間表現を示す
 */
public class Variable extends SymbolDefinition
{

    /** 変数名：プリプロセッサシンボルの正規表現 */
    static public final Pattern REGEX_PREPROCESSOR_PREFIX = Pattern.compile( "^[a-z|A-Z|_]" );

    /** 元となるASTノード */
    public final ASTVariableDeclaration astNode;

    /** 配列型の場合の要素数 */
    public int arraySize = 0;

    /**
     * UI型変数の場合に値がセットされる（シンボル収集フェーズ）
     * 初期値は null
     * @see SymbolCollector
     */
    public UIType uiTypeInfo = null;

    /** コンスタントプールに格納される場合のインデックス番号 */
    public int constantIndex = -1;

    /** on init 内で使用可能な変数かどうか（外部低ファイルから読み込むビルトイン変数用） */
    public boolean availableOnInit = true;

    /** 意味解析フェーズ中に走査し参照されたかを記録する */
    public boolean referenced = false;

    /** 単項演算子により生成、かつリテラル値 */
    public boolean constantValueWithSingleOperator = false;

    /** 状態 */
    public VariableState status = VariableState.UNLOADED;

    /**
     * Ctor.
     */
    public Variable( ASTVariableDeclaration node )
    {
        copy( node.symbol, this );
        this.astNode    = node;
        this.symbolType = SymbolType.Variable;
    }

    /**
     * 変数名の1文字目の記号から型情報を算出する
     */
    public boolean setTypeFromVariableName()
    {
        this.type = getKSPTypeFromVariableName( this.name );
        return this.type != TYPE_NONE;
    }

    /**
     * valueに指定された値を割り当てる
     */
    public void setValue( Object v )
    {
        this.value = v;
    }

    /**
     * valueに指定された値のIntegerインスタンスを割り当てる
     */
    public void setValue( int v )
    {
        if( !isInt() )
        {
            throw new IllegalStateException( "Type is " + getTypeName() );
        }
        this.value = new Integer( v );
    }

    /**
     * valueに指定された値のint[]インスタンスを割り当てる
     */
    public void setValue( int[] v )
    {
        if( !isInt() || !isArray() )
        {
            throw new IllegalStateException( "Type is " + getTypeName() );
        }
        int[] newV = Arrays.copyOf( v, v.length );
        this.value = newV;
    }

    /**
     * valueに指定された値のDoubleインスタンスを割り当てる
     */
    public void setValue( double v )
    {
        if( !isReal() )
        {
            throw new IllegalStateException( "Type is " + getTypeName() );
        }
        this.value = new Double( v );
    }

    /**
     * valueに指定された値のint[]インスタンスを割り当てる
     */
    public void setValue( double[] v )
    {
        if( !isReal() || !isArray() )
        {
            throw new IllegalStateException( "Type is " + getTypeName() );
        }
        double[] newV = Arrays.copyOf( v, v.length );
        this.value = newV;
    }

    /**
     * valueに指定された値のStringインスタンスを割り当てる
     */
    public void setValue( String v )
    {
        if( !isString() )
        {
            throw new IllegalStateException( "Type is " + getTypeName() );
        }
        this.value = v;
    }

    /**
     * valueに指定された値のint[]インスタンスを割り当てる
     */
    public void setValue( String[] v )
    {
        if( !isString() || !isArray() )
        {
            throw new IllegalStateException( "Type is " + getTypeName() );
        }
        String[] newV = Arrays.copyOf( v, v.length );
        this.value = newV;
    }

    /**
     * valueに指定された値のBooleanインスタンスを割り当てる
     */
    public void setValue( Boolean v )
    {
        if( !isBoolean() )
        {
            throw new IllegalStateException( "Type is " + getTypeName() );
        }
        this.value = v;
    }

    /**
     * valueに指定された値のboolean[]インスタンスを割り当てる
     */
    public void setValue( boolean[] v )
    {
        if( !isBoolean() || !isArray() )
        {
            throw new IllegalStateException( "Type is " + getTypeName() );
        }
        boolean[] newV = Arrays.copyOf( v, v.length );
        this.value = newV;
    }

    /**
     * 定数かどうかを判定する
     */
    public boolean isConstant()
    {
        return isConstant( accessFlag );
    }

    /**
     * 与えられた属性フラグ情報から定数かどうかを判定する
     */
    static public boolean isConstant( int accessFlag )
    {
        return ( accessFlag & ACCESS_ATTR_CONST ) != 0;
    }

    /**
     * 数値型かどうかを判定する
     */
    public boolean isNumeric()
    {
        switch( type & TYPE_MASK )
        {
            case TYPE_INT:
            case TYPE_REAL:
                return true;

            default:
                return false;
        }
    }

    /**
     * この変数の型がIntegerかどうかを判別する
     */
    public boolean isInt()
    {
        return isInt( getPrimitiveType() );
    }

    /**
     * 与えられた型識別値がIntegerかどうかを判別する
     */
    static public boolean isInt( int type )
    {
        return ( type & TYPE_INT ) != 0;
    }

    /**
     * この変数の型がRealかどうかを判別する
     */
    public boolean isReal()
    {
        return isReal( getPrimitiveType() );
    }

    /**
     * 与えられた型識別値がRealかどうかを判別する
     */
    static public boolean isReal( int type )
    {
        return ( type & TYPE_REAL ) != 0;
    }

    /**
     * この変数の型がStringかどうかを判別する
     */
    public boolean isString()
    {
        return isString( getPrimitiveType() );
    }

    /**
     * 与えられた型識別値がIntegerかどうかを判別する
     */
    static public boolean isString( int type )
    {
        return ( type & TYPE_STRING ) != 0;
    }

    /**
     * この変数の型がBooleanかどうかを判別する
     */
    public boolean isBoolean()
    {
        return isBoolean( getPrimitiveType() );
    }

    /**
     * 与えられた型識別値がbooleanかどうかを判別する
     */
    static public boolean isBoolean( int type )
    {
        return ( type & TYPE_BOOL ) != 0;
    }

    /**
     * この変数の型が配列属性が付いているかどうかを判別する
     */
    public boolean isArray()
    {
        return isArray( type );
    }

    /**
     * 与えられた型識別値が配列属性を含むかどうかを判別する
     */
    static public boolean isArray( int type )
    {
        return ( type & TYPE_ATTR_ARRAY ) != 0;
    }

    /**
     * UI変数かどうかを判定する
     */
    public boolean isUIVariable()
    {
        return isUIVariable( accessFlag );
    }

    /**
     * 与えられた属性フラグ情報からUI変数かどうかを判定する
     */
    static public boolean isUIVariable( int accessFlag )
    {
        return ( accessFlag & ACCESS_ATTR_UI ) != 0;
    }

    /**
     * ポリフォニック変数かどうかを判定する
     */
    public boolean isPolyphonicVariable()
    {
        return isPolyphonicVariable( accessFlag );
    }

    /**
     * 与えられた属性フラグ情報からポリフォニック変数かどうかを判定する
     */
    static public boolean isPolyphonicVariable( int accessFlag )
    {
        return ( accessFlag & ACCESS_ATTR_POLY ) != 0;
    }

    /**
     * #TYPE_VOID フラグがONかどうかを判定する
     */
    public boolean isVoid()
    {
        return isVoid( type );
    }

    /**
     * 与えられた型識別値がVoidかどうかを判別する
     */
    static public boolean isVoid( int type )
    {
        return ( type & TYPE_VOID ) != 0;
    }

    /**
     * 数値型かどうかを判定する
     */
    public boolean isNumeral()
    {
        return isNumeral( type );
    }

    /**
     * 与えられた型識別値から数値型かどうかを判定する
     */
    static public boolean isNumeral( int t )
    {
        switch( t & TYPE_MASK )
        {
            case TYPE_INT:
            case TYPE_REAL:
                return true;
            default:
                return false;
        }
    }

    /**
     * 与えられた変数名から数値型かどうかを判定する
     */
    static public boolean isNumeralFromVariableName( String variableName )
    {
        return isNumeral( getKSPTypeFromVariableName( variableName ) );
    }

    /**
     * 配列情報フラグ等を含まない、純粋なプリミティブ型の識別値を取得する。
     * 型の識別値のビットフラグを返す個別の判定は isInt()、isReal()、isString() 等を使用すること。
     */
    public int getPrimitiveType()
    {
        return type & TYPE_MASK;
    }

    /**
     * 型の付随識別情報のビットフラグを返す。個別の判定は isArray() 等を使用すること。
     */
    public int getTypeAttribute()
    {
        return type & TYPE_ATTR_MASK;
    }

    /**
     * 変数名を元に文字列表現された型情報を返す
     */
    public String getTypeName()
    {
        return getTypeName( name );
    }

    /**
     * KSPの変数名を元に文字列表現された型情報を返す
     */
    static public String getTypeName( int type )
    {
        return getTypeName( toKSPTypeCharacter( type ) );
    }

    /**
     * KSPの変数名を元に文字列表現された型情報を返す
     */
    static public String getTypeName( String name )
    {
        if( name == null || name.length() == 0 )
        {
            return "Unknown";
        }

        char t = name.charAt( 0 );
        switch( t )
        {
            case '$': return "Integer";
            case '%': return "Integer Array";
            case '~': return "Real";
            case '?': return "Real Array";
            case '@': return "String";
            case '!': return "String Array";
            //--------------------------------------------------------------------------
            // 内部処理用
            //--------------------------------------------------------------------------
            case 'B': return "bool";
            case 'V': return "void";
            case 'P': return "preprocessor";
            case '*': return "any";
            default:
                if( REGEX_PREPROCESSOR_PREFIX.matcher( name ).find() )
                {
                    return "Preprocessor Symbol";
                }
                return "Unknown";
        }
    }

    /**
     * 変数名の1文字目の記号から型情報を算出する
     */
    static public int getKSPTypeFromVariableName( String variableName )
    {
        if( variableName == null || variableName.length() == 0 )
        {
            return TYPE_NONE;
        }
        char t = variableName.charAt( 0 );

        if( REGEX_PREPROCESSOR_PREFIX.matcher( variableName ).find() )
        {
            return TYPE_PREPROCESSOR_SYMBOL;
        }

        switch( t )
        {
            case '$': return TYPE_INT;
            case '%': return TYPE_INT | TYPE_ATTR_ARRAY;
            case '~': return TYPE_REAL;
            case '?': return TYPE_REAL | TYPE_ATTR_ARRAY;
            case '@': return TYPE_STRING;
            case '!': return TYPE_STRING | TYPE_ATTR_ARRAY;
            //--------------------------------------------------------------------------
            // 内部処理用
            //--------------------------------------------------------------------------
            case 'B': return TYPE_BOOL;
            case 'V': return TYPE_VOID;
            case 'P': return TYPE_PREPROCESSOR_SYMBOL;
            case '*': return TYPE_ANY;

            default:
                throw new IllegalArgumentException( "unknown ksp type : " + String.valueOf( t ) + ":" + variableName );
        }
    }

    /**
     * 変数の型データからKSPの定める型式別の記号に変換する
     */
    public String toKSPTypeCharacter()
    {
        return toKSPTypeCharacter( type );
    }

    /**
     * 変数の型データからKSPの定める型式別の記号に変換する
     */
    static public String toKSPTypeCharacter( int type )
    {
        switch( type )
        {
            case TYPE_INT:                      return "$";
            case TYPE_INT | TYPE_ATTR_ARRAY:    return "%";
            case TYPE_REAL:                     return "~";
            case TYPE_REAL | TYPE_ATTR_ARRAY:   return "?";
            case TYPE_STRING:                   return "@";
            case TYPE_STRING | TYPE_ATTR_ARRAY: return "!";
            //--------------------------------------------------------------------------
            // 内部処理用
            //--------------------------------------------------------------------------
            case TYPE_BOOL:                     return "B";
            case TYPE_VOID:                     return "V";
            case TYPE_PREPROCESSOR_SYMBOL:      return "P";
            case TYPE_ANY:                      return "*";
            default:
                throw new IllegalArgumentException( "type is " + type );
        }
    }

    /**
     * 変数の型データからKSPの定める型式別の記号に変換する
     */
    static public String toKSPTypeName( int type )
    {
        return getTypeName( toKSPTypeCharacter( type ) );
    }
}
