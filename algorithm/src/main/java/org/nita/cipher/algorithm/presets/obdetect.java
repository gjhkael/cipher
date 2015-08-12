package org.nita.cipher.algorithm.presets;

import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.tools.Info;
import org.bytedeco.javacpp.tools.InfoMap;
import org.bytedeco.javacpp.tools.InfoMapper;

/**
 * Created by guojianhua on 3/28/15.
 */
@Properties( target="org.nita.cipher.algorithm.obdetect", value={@Platform(include="<obdetect.h>",includepath="/usr/local/include/",link="obdetect@.so")})
public class obdetect implements InfoMapper {
    public void map(InfoMap infoMap) {
        infoMap.put(new Info("cv::Mat").cast().pointerTypes("BytePointer"));
        infoMap.put(new Info("std::string").cast().valueTypes("String"));
    }
}

