/* =========================================================================

    AnalyzerConstants.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.regex.Pattern;

/**
 * シンボル収集、意味解析フェーズ中に使用する共通の定数
 */
public interface AnalyzerConstants
{

    /** VM引数 -D dataフォルダの明示的指定時のプロパティ名 */
    String SYSTEM_PROPERTY_DATADIR = "kspparser.datadir";

    /** アトリビュート：なし */
    int ACCESS_ATTR_NONE = 0x00;

    /** アトリビュート：読み取り専用 */
    int ACCESS_ATTR_CONST = 0x01;

    /** アトリビュート：ポリフォニック変数 */
    int ACCESS_ATTR_POLY = 0x02;

    /** アトリビュート：UI変数 */
    int ACCESS_ATTR_UI  = 0x04;

    /** アトリビュート：コールバック */
    int ACCESS_ATTR_CALLBACK = 0x100;

    /** アトリビュート：ユーザー定義関数 */
    int ACCESS_ATTR_USER_FUNCTION = 0x200;

    /**
     * コンパイル時に使用する、代入演算子の識別子。
     */
    public enum AssignOprator
    {
        /** 初期値 */
        NULL,

        /** := */
        ASSIGN,
    };

// 型情報は32bitで構成
/*
    0b 00000000 00000000 00000000 00000000
       ----+--- ---------+----------------
           |             |
           |             +-- bit 0-23 データ型
           |
           +---------------- bit 24-31 配列等の補足情報
*/
    int TYPE_BIT_SIZE      = 24;
    int TYPE_ATTR_BIT_SIZE = 8;

    //--------------------------------------------------------------------------
    // 基本型情報 (0x00000000-0x00ffffff)
    //--------------------------------------------------------------------------
    int TYPE_MASK                   = 0x00ffffff;
    int TYPE_NONE                   = 0x00;
    int TYPE_INT                    = 0x01;
    int TYPE_STRING                 = 0x02;
    int TYPE_REAL                   = 0x04;
    int TYPE_BOOL                   = 0x100;
    int TYPE_VOID                   = 0x200;
    int TYPE_PREPROCESSOR_SYMBOL    = 0x400;
    int TYPE_KEYID                  = 0x800; // e.g. PGS <key-id>

    int TYPE_NUMERICAL              = TYPE_INT | TYPE_REAL;
    int TYPE_NON_VARIABLE           = TYPE_PREPROCESSOR_SYMBOL | TYPE_KEYID;
    int TYPE_MULTIPLE               = 0xffffff; // 型識別全ビット ON

    //--------------------------------------------------------------------------
    // 配列などの情報フラグ (0x01000000~0xff000000)
    //--------------------------------------------------------------------------
    int TYPE_ATTR_MASK  = 0xff000000;
    // 配列
    int TYPE_ATTR_ARRAY = 0x01000000;
    // e.g. type is int[]
    // type = TYPE_INT | TYPE_ATTR_ARRAY
    // if( ( type & TYPE_ATTR_MASK ) == TYPE_ATTR_ARRAY )
    // {
    //     // type is int[]
    // }

    // 全情報ビットフラグON
    int TYPE_ATTR_ANY = 0x7F000000;
    int TYPE_ALL      = 0xffffffff;

    //--------------------------------------------------------------------------
    // 上限・下限
    // 32bit signed
    //--------------------------------------------------------------------------

    /** 整数値の下限 */
    int KSP_INT_MIN = Integer.MIN_VALUE;
    /** 整数値の上限 */
    int KSP_INT_MAX = Integer.MAX_VALUE;

    /** 浮動小数値の下限 */
    float KSP_REAL_MIN = Float.MIN_VALUE;
    /** 浮動小数値の上限 */
    float KSP_REAL_MAX = Float.MAX_VALUE;

    /** 配列変数宣言時の要素数の上限 */
    int MAX_KSP_ARRAY_SIZE = 32768;

    /*
     * 変数の状態を示す識別子。
     */
    public enum SymbolState
    {
        /** 未初期化 */
        UNLOADED,

        /** 代入中 */
        LOADING,

        /** 代入済み */
        LOADED,

        /** 初期化済み */
        INITIALIZED,
    }

    //-------------------------------------------------------------------------------
    // コールバック・ユーザー定義関数識別
    //-------------------------------------------------------------------------------
    public enum FunctionType
    {
        CALLBACK,
        USER_FUNCTION,
    }

    //-------------------------------------------------------------------------------
    // コンスタントプール
    //-------------------------------------------------------------------------------

    /** コンスタントプール・タグ情報：整数 */
    int CONSTAT_TAG_INT    = 0;

    /** コンスタントプール・タグ情報：文字列 */
    int CONSTAT_TAG_STRING = 1;

    //--------------------------------------------------------------------------
    // シンボル名検証
    //--------------------------------------------------------------------------

    /** 変数名の1文字目に数字文字が含まれているかどうか。KSPは許容するが、一般的な言語ではNG */
    Pattern REGEX_NUMERIC_PREFIX = Pattern.compile( "^.[0-9]" );

    /** 変数名：データ型記号付いているシンボルの正規表現 */
    Pattern REGEX_TYPE_PREFIX = Pattern.compile( "^[\\$|\\%|\\@|\\!\\?|\\~]" );

    /** 変数名：データ型記号がつかないプリプロセッサシンボル等のシンボルの正規表現 */
    Pattern REGEX_NON_TYPE_PREFIX = Pattern.compile( "^[a-z|A-Z|_]" );
}
