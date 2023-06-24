package com.imooc.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 使用mq减少库存
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DecreaseStock {
    private String ItemSpecId;

    private Integer buyCounts;
}
