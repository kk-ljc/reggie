package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和菜品数据的关联数据
     */
    void removeWithDish(List<Long> ids);

    //根据id查询套餐信息和对应的菜品信息
    SetmealDto getByIdWithDish(Long id);

    //更新套餐信息，同时更新对应的菜品信息
    void updateWithDish(SetmealDto setmealDto);
}
