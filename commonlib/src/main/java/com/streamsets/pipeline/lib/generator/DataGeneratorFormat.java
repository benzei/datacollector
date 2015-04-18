/**
 * (c) 2015 StreamSets, Inc. All rights reserved. May not
 * be copied, modified, or distributed in whole or part without
 * written consent of StreamSets, Inc.
 */
package com.streamsets.pipeline.lib.generator;

import com.streamsets.pipeline.api.impl.Utils;
import com.streamsets.pipeline.lib.data.DataFactory;
import com.streamsets.pipeline.lib.data.DataFormat;
import com.streamsets.pipeline.lib.generator.delimited.DelimitedDataGeneratorFactory;
import com.streamsets.pipeline.lib.generator.json.JsonDataGeneratorFactory;
import com.streamsets.pipeline.lib.generator.sdcrecord.SdcRecordDataGeneratorFactory;
import com.streamsets.pipeline.lib.generator.text.TextDataGeneratorFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

public enum DataGeneratorFormat implements DataFormat<DataGeneratorFactory> {
  TEXT(TextDataGeneratorFactory.class, TextDataGeneratorFactory.MODES, TextDataGeneratorFactory.CONFIGS),
  JSON(JsonDataGeneratorFactory.class, JsonDataGeneratorFactory.MODES, JsonDataGeneratorFactory.CONFIGS),
  DELIMITED(DelimitedDataGeneratorFactory.class, DelimitedDataGeneratorFactory.MODES,
    DelimitedDataGeneratorFactory.CONFIGS),
  SDC_RECORD(SdcRecordDataGeneratorFactory.class, SdcRecordDataGeneratorFactory.MODES,
    SdcRecordDataGeneratorFactory.CONFIGS),
  ;

  private final Class<? extends DataGeneratorFactory> klass;
  private final Constructor<? extends DataGeneratorFactory> constructor;
  private final Set<Class<? extends Enum>> modes;
  private Map<String, Object> configs;

  DataGeneratorFormat(Class<? extends DataGeneratorFactory> klass, Set<Class<? extends Enum>> modes,
                   Map<String, Object> configs) {
    this.klass = klass;
    try {
      constructor = klass.getConstructor(DataFactory.Settings.class);
      Utils.checkState((constructor.getModifiers() & Modifier.PUBLIC) != 0,
        Utils.formatL("Constructor for DataFactory '{}' must be public",
          klass.getName()));
    } catch (Exception ex) {
      throw new RuntimeException(Utils.format("Could not obtain constructor '<init>({})' for DataFactory '{}': {}",
        DataFactory.Settings.class, klass.getName(), ex.getMessage()), ex);
    }
    this.modes = modes;
    this.configs = configs;

  }

  @Override
  public Set<Class<? extends Enum>> getModes() {
    return modes;
  }

  @Override
  public Map<String, Object> getConfigs() {
    return configs;
  }

  @Override
  public DataGeneratorFactory create(DataFactory.Settings settings) {
    try {
      return constructor.newInstance(settings);
    } catch (Exception ex) {
      throw new RuntimeException(Utils.format("Could not create DataFactory instance for '{}': {}",
        klass.getName(), ex.getMessage(), ex));
    }
  }
}
