/* =========================================================================

    SymbolDefinition.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.Arrays;

/**
 * ASTノード、ノード中に宣言されたシンボルの中間状態を表現する
 */
public class SymbolDefinition implements AnalyzerConstants
{
    public enum SymbolType
    {
        Unknown,
        Callback,
        Command,
        UserFunction,
        Variable,
        Literal,
        Expression,
        PreprocessorSymbol,
    };

    /** シンボルの種類 */
    public SymbolType symbolType = SymbolType.Unknown;
    /** シンボルテーブルインデックス値 */
    public int index = -1;
    /** データ型 */
    public int type = TYPE_NONE;
    /** アクセス識別フラグ（ある場合に使用。未使用の場合は0） */
    public int accessFlag = 0;
    /** 実行環境で予約済みのシンボルかどうか） */
    public boolean reserved = false;
    /** 識別子名 */
    private String name = "";
    /** オブファスケート後の識別子名 */
    private String obfuscatedName = "";
    /** 状態 */
    public SymbolState state = SymbolState.UNLOADED;
    /** 意味解析フェーズ中に走査し参照されたかを記録する */
    public boolean referenced = false;
    /** 意味解析フェーズ中に走査し参照された回数を記録する */
    public int referenceCount = 0;
    /** accessFlagにACCESS_ATTR_UIが含まれている場合のUIタイプの識別子名 */
    public String uiTypeName = "";
    /** 値がある場合はその値(Integer,Double,String,int[],double[],String[]) */
    public Object value = null;
    /** 定義した行・列情報 */
    public final Position position = new Position();

    /** ワークバッファ */
    static private final StringBuilder stringBuffer = new StringBuilder( 512 );

    /**
     * Ctor.
     */
    public SymbolDefinition(){}

    /**
     * コピーコンストラクタ
     */
    public SymbolDefinition( SymbolDefinition src )
    {
        SymbolDefinition.copy( src, this );
    }

    /**
     * 値コピー
     */
    static public void copy( SymbolDefinition src, SymbolDefinition dest )
    {
        if( src == dest )
        {
            // 参照先が同じなので何もしない
            return;
        }
        dest.symbolType     = src.symbolType;
        dest.index          = src.index;
        dest.type           = src.type;
        dest.accessFlag     = src.accessFlag;
        dest.reserved       = src.reserved;
        dest.name           = src.name;
        dest.uiTypeName     = src.uiTypeName;
        dest.value          = src.value;
        dest.position.copy( src.position );
    }

    /**
     * このシンボルのダンプ情報を取得する
     */
    public String dump()
    {
        StringBuilder sb = stringBuffer;
        sb.delete( 0, sb.length() );
        sb.append( "---- Symbol Information -----------------------------" ).append( '\n' );
        sb.append( "symbolType:" ).append( symbolType ).append( '\n' );
        sb.append( "index:" ).append( index ).append( '\n' );
        sb.append( "type:" ).append( getTypeName( type ) ).append( '\n' );
        sb.append( "accessFlag:" ).append( "0x" ).append( Integer.toHexString( accessFlag ) ).append( '\n' );
        sb.append( "reserved:" ).append( reserved ).append( '\n' );
        sb.append( "name:" ).append( name ).append( '\n' );
        sb.append( "obfuscatedName:" ).append( obfuscatedName ).append( '\n' );
        sb.append( "uiTypeName:" ).append( uiTypeName ).append( '\n' );
        sb.append( "value:" ).append( value ).append( '\n' );

        return sb.toString();
    }

    /**
     * このシンボル名を取得する。
     */
    public String getName()
    {
        if( obfuscatedName != null && obfuscatedName.length() > 0 )
        {
            return obfuscatedName;
        }
        return name;
    }

    /**
     * このシンボル名を取得する。
     * @param originalName オブファスケート前のシンボル名を取得するかどうか
     */
    public String getName( boolean originalName )
    {
        if( originalName )
        {
            return name;
        }
        return obfuscatedName;
    }

    /**
     * オブファスケートされたシンボル名を取得する
     */
   public String getObfuscatedName()
   {
       return obfuscatedName;
   }

    /**
     * このシンボル名を設定する。
     */
    public void setName( String newName ) throws NullPointerException
    {
        if( newName == null )
        {
            throw new NullPointerException( "newName is null" );
        }
        name = newName;
    }

    /**
     * このシンボルのオブファスケート後のシンボル名を設定する。
     */
    public void setObfuscatedName( String newName ) throws NullPointerException
    {
        if( newName == null )
        {
            throw new NullPointerException( "newName is null" );
        }
        obfuscatedName = newName;
    }

    /**
     * シンボル名の1文字目の記号から変数の型情報を算出する
     */
    public boolean setTypeFromVariableName()
    {
        this.type = getKSPTypeFromVariableName( this.name );
        return this.type != TYPE_NONE;
    }

    /**
     * valueに指定された値を割り当てる(フィールド変数valueの浅いコピー)
     */
    static public void setValue( SymbolDefinition src, SymbolDefinition dest )
    {
        dest.value = src.value;
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
     * このシンボルの型がIntegerかどうかを判別する
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
     * このシンボルの型がRealかどうかを判別する
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
     * このシンボルの型がStringかどうかを判別する
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
     * このシンボルの型がBooleanかどうかを判別する
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
     * このシンボルの型が配列属性が付いているかどうかを判別する
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
     * このシンボルが持つ型識別値が複数のデータ型ビットがONになっているかどうかを判定する
     * 戻り値など、暗黙の型変換の可能性がある式やコマンドで使用する。
     */
    public boolean hasMultipleType()
    {
        return hasMultipleType( type );
    }

    /**
     * 与えられた型識別値から複数のデータ型ビットがONになっているかどうかを判定する
     * 戻り値など、暗黙の型変換の可能性がある式やコマンドで使用する。
     */
    static public boolean hasMultipleType( int type )
    {
        int cnt = 0;
        for( int i = 0; i < TYPE_BIT_SIZE; i++ )
        {
            if( ( type & ( 1 << i ) ) != 0 )
            {
                cnt++;
            }
        }
        return cnt >= 2;
    }

    /**
     * プリプロセッサシンボルなど、データ型(数値・文字列)に
     * 当てはまらないデータ型かどうかを判定する
     */
    public boolean isNonVariebleType()
    {
        return isNonVariebleType( type );
    }

    /**
     * プリプロセッサシンボルなど、データ型(数値・文字列)に
     * 当てはまらないデータ型かどうかを判定する
     */
    static public boolean isNonVariebleType( int type )
    {
        return ( type & TYPE_NON_VARIABLE ) != 0;
    }

    /**
     * プリプロセッサシンボルなど、データ型(数値・文字列)に
     * 当てはまらないデータ型の1文字目の検証を行う（[0-9]以外で始まるかどうか）
     */
    public boolean validateNonVariablePrefix()
    {
        return validateNonVariablePrefix( name );
    }

    /**
     * プリプロセッサシンボルなど、データ型(数値・文字列)に
     * 当てはまらないデータ型の1文字目の検証を行う（[0-9]以外で始まるかどうか）
     */
    static public boolean validateNonVariablePrefix( String name )
    {
        return REGEX_NON_TYPE_PREFIX.matcher( name ).find();
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
     * 配列情報フラグ等を含まない、純粋なプリミティブ型の識別値を取得する。
     * 型の識別値のビットフラグを返す個別の判定は isInt()、isReal()、isString() 等を使用すること。
     */
    static public int getPrimitiveType( int type )
    {
        return type & TYPE_MASK;
    }

    /**
     * 型識別情報のビットフラグを明示的に指定する
     * @param typeFlag TYPE_#### TYPE_ATTR_####
     * @param accessFlag ACCESS_ATTR_####
     */
    public void setTypeFlag( int typeFlag, int accessFlag )
    {
        this.type       = typeFlag;
        this.accessFlag = accessFlag;
    }

    /**
     * 型識別情報のビットフラグを明示的に指定する
     */
    static public void setTypeFlag( SymbolDefinition src, SymbolDefinition dest )
    {
        dest.setTypeFlag( src.type,  src.accessFlag );
    }

    /**
     * 型の付随識別情報のビットフラグに指定されたフラグを元に該当ビットをONにする
     * @param typeFlag TYPE_#### TYPE_ATTR_####
     * @param accessFlag ACCESS_ATTR_####
     */
    public void addTypeFlag( int typeFlag, int accessFlag )
    {
        this.type       |= typeFlag;
        this.accessFlag |= accessFlag;
    }

    /**
     * 型の付随識別情報のビットフラグに指定されたフラグを元に該当ビットをOFFにする
     * @param typeFlag TYPE_ATTR_####
     * @param accessFlag ACCESS_ATTR_####
     */
    public void removeTypeFlag( int typeFlag, int accessFlag )
    {
        this.type       &= ~typeFlag;
        this.accessFlag &= ~accessFlag;
    }

    /**
     * 型の付随識別情報のビットフラグを返す。個別の判定は isArray() 等を使用すること。
     */
    public int getTypeAttribute()
    {
        return type & TYPE_ATTR_MASK;
    }

    /** value の初期値のダミー  */
    static public final Object DEFAULT_VALUE_DUMMY = new Object();

    /**
     * シンボルの型データから初期値を生成し戻り値として返す
     * @return Integer, Double, String, Boolean インスタンスのいずれか。typeが該当しない場合は DEFAULT_VALUE_DUMMY
     */
    public Object getDefaultValue()
    {
        return getDefaultValue( type );
    }

    /**
     * シンボルの型データから初期値を生成し戻り値として返す
     * @return Integer, Double, String インスタンスのいずれか。
     */
    static public Object getDefaultValue( int type )
    {
        switch( getPrimitiveType( type ) )
        {
            case TYPE_INT:                      return new Integer( 0 );
            case TYPE_REAL:                     return new Double( 0 );
            case TYPE_STRING:                   return "";
            //--------------------------------------------------------------------------
            // 内部処理用
            //--------------------------------------------------------------------------
            case TYPE_BOOL:                     return new Boolean( true );
            default:
                return DEFAULT_VALUE_DUMMY;
        }
    }

    /**
     * シンボル名を元に文字列表現された型情報を返す
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
            case 'B': return "Boolean";
            case 'V': return "Void";
            case 'P': return "Preprocessor";
            case 'K': return "KeyID";
            case '*': return "any";
            default:
                if( AnalyzerConstants.REGEX_NON_TYPE_PREFIX.matcher( name ).find() )
                {
                    return "Preprocessor Symbol or Key ID";
                }
                return "Unknown";
        }
    }

    /**
     * シンボル名の1文字目の記号から型情報を算出する
     */
    static public int getKSPTypeFromVariableName( String variableName )
    {
        if( variableName == null || variableName.length() == 0 )
        {
            return TYPE_NONE;
        }
        char t = variableName.charAt( 0 );

        if( AnalyzerConstants.REGEX_NON_TYPE_PREFIX.matcher( variableName ).find() )
        {
            return TYPE_PREPROCESSOR_SYMBOL | TYPE_KEYID;
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
            case 'K': return TYPE_KEYID;
            case '*': return TYPE_MULTIPLE;

            default:
                throw new IllegalArgumentException( "unknown ksp type : " + String.valueOf( t ) + ":" + variableName );
        }
    }

    /**
     * シンボルの型データからKSPの定める型式別の記号に変換する
     */
    public String toKSPTypeCharacter()
    {
        return toKSPTypeCharacter( type );
    }

    /**
     * シンボルの型データからKSPの定める型式別の記号に変換する
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
            case TYPE_KEYID:                    return "K";
            case TYPE_MULTIPLE:                      return "*";
            default:
                return "{UNKNOWN:" + type  + "}";
        }
    }

    /**
     * シンボルの型データからKSPの定める型式別の記号に変換する
     */
    static public String toKSPTypeName( int type )
    {
        return getTypeName( toKSPTypeCharacter( type ) );
    }

    /**
     * シンボル名表現に変換する
     */
    @Override
    public String toString()
    {
        return name;
    }
}
