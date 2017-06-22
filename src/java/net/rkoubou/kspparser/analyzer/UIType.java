/* =========================================================================

    UIType.java
    Copyright (c) R-Koubou

   ======================================================================== */

package net.rkoubou.kspparser.analyzer;

import java.util.Arrays;

/**
 * 変数宣言時：UIの種類を識別するための中間表現を示す
 */
public class UIType
{

    /** UIの種類 ui_###### */
    public final String name;

    /**
     * UI変数の「値を格納するデータ型」
     * <p>
     * ui_knob の場合、変数は $xxxx (int) だが、格納する値は int[] である。
     * その場合、変数の型はこのクラスが持つデータ型が優先される。
     * </p>
     */
    public final int uiValueType;

    /** 実行環境で予約済みのシンボルかどうか） */
    public final boolean reserved;

    /** 値代入が可能かどうか（定数扱いにするかどうか） */
    public final boolean constant;

    /** 初期値代入が必須かどうか */
    public final boolean initializerRequired;

    /** 初期値代入が必要な場合の初期値の型リスト */
    public int[] initilzerTypeList = new int[ 0 ];

    /** シンボルテーブルインデックス値 */
    public int index = -1;

    /**
     * Ctor.
     */
    public UIType( String name, boolean reserved, int uiValueType, Boolean constant, boolean initializerRequired, int[] typeList )
    {
        if( name == null )
        {
            throw new NullPointerException( "name is null" );
        }
        this.name                   = name;
        this.uiValueType            = uiValueType;
        this.reserved               = reserved;
        this.constant               = constant;
        if( initializerRequired && typeList != null && typeList.length > 0 )
        {
            this.initilzerTypeList = Arrays.copyOf( initilzerTypeList, initilzerTypeList.length );
        }
        this.initializerRequired    = initializerRequired;
    }
}
