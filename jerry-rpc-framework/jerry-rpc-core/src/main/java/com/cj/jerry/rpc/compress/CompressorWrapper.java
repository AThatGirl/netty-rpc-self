package com.cj.jerry.rpc.compress;

import com.cj.jerry.rpc.serialize.Serializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompressorWrapper {

    private byte code;
    private String type;
    private Compressor compressor;

}
