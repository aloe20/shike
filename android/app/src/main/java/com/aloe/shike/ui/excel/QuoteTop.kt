package com.aloe.shike.ui.excel

/**
 * 股票数据列.
 * @param showName 列名
 */
@Suppress("unused")
enum class QuoteTop(val showName: String) {
    /**
     * 最新价.
     */
    LAST_PRICE("最新价"),

    /**
     * 涨幅.
     */
    UP_DOWN("涨幅"),

    /**
     * 换手.
     */
    TURNOVER_RATE("换手"),

    /**
     * 总量.
     */
    VOLUME("总量"),

    /**
     * 金额.
     */
    AMOUNT("金额"),

    /**
     * 量比.
     */
    RATIO("量比"),

    /**
     * 振幅.
     */
    SA("振幅"),

    /**
     * 涨速.
     */
    UP_DOWN_SPEED("涨速"),

    /**
     * 市盈率.
     */
    PE_RATIO("市盈率"),

    /**
     * 总市值.
     */
    TOT_VAL("总市值"),

    /**
     * 流通市值.
     */
    CIR_VAL("流通市值"),

    /**
     * 主力流出.
     */
    FLOW_MAIN_OUT("主力流出"),

    /**
     * 主力流入.
     */
    FLOW_MAIN_IN("主力流入"),

    /**
     * 主力净注入.
     */
    FLOW_MAIN_NET_IN("主力净注入")
}
