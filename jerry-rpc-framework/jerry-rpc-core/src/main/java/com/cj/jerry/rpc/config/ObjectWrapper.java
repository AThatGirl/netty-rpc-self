package com.cj.jerry.rpc.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ObjectWrapper<T> {

    private Byte code;
    private String name;
    private T impl;

}
