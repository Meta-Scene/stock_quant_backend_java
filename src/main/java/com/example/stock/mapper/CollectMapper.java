package com.example.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.stock.entity.Collect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CollectMapper extends BaseMapper<Collect> {
    
    /**
     * 查询所有收藏的股票代码
     * @return 股票代码列表
     */
    @Select("SELECT ts_code FROM collect ORDER BY create_time")
    List<String> findAllTsCodes();
    
    /**
     * 根据股票代码查询是否已收藏
     * @param tsCode 股票代码
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM collect WHERE ts_code = #{tsCode}")
    boolean existsByTsCode(String tsCode);
    
    /**
     * 查询是否有对应的股票代码
     * @param tsCode 股票代码
     * @return 是否存在
     */
    @Select("SELECT EXISTS(SELECT 1 FROM all_stocks_days WHERE ts_code = #{tsCode})")
    boolean isStockExist(String tsCode);
} 