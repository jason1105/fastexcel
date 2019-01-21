package com.github.sd4324530.fastexcel.entity;

import com.github.sd4324530.fastexcel.annotation.MapperCell;

public class MenuObject {

    @MapperCell(cellName = "一级", order = 0)
    public String level1;
    @MapperCell(cellName = "二级", order = 0)
    public String level2;
    @MapperCell(cellName = "三级", order = 0)
    public String level3;
    @MapperCell(cellName = "四级", order = 0)
    public String level4;

    @Override
    public String toString() {
        return level1 + " " + level2 + " " + level3 + " " + level4;
    }
}
