package com.consoledeployserver.model;

import lombok.Data;

@Data
public class PageInfo {

    private int totalPage;

    private int currentPage;

    private String content;

    /**
     * 传入总的行数和每页展示的行数进行分页
     * @param size
     * @param pageCount
     */
    public void CalTotalPage(int size, int pageCount){
        this.totalPage = size % pageCount == 0 ? size / pageCount : (size / pageCount) + 1;
    }
}
