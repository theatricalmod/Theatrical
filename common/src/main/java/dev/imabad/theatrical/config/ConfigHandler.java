package dev.imabad.theatrical.config;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.config.api.TheatricalConfigItem;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.inspector.TagInspector;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ConfigHandler {

    public static ConfigHandler INSTANCE;
    public static ConfigHandler initialize(Path configFolder){
        INSTANCE = new ConfigHandler(configFolder);
        return INSTANCE;
    }

    public enum ConfigSide {
        COMMON,
        CLIENT
    }

    private final Path configFolder;
    private final Yaml yaml;

    private final Map<ResourceLocation, BaseConfig> registered_configs = new HashMap<>();

    private ConfigHandler(Path configFolder){
        this.configFolder = configFolder;
        File file = this.configFolder.toFile();
        if(!file.exists()){
            file.mkdirs();
        }
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        LoaderOptions loaderOptions = new LoaderOptions();
        TagInspector taginspector =
                tag -> tag.getClassName().equals(UniverseConfig.class.getName());
        loaderOptions.setTagInspector(taginspector);
        this.yaml = new Yaml(new Constructor(loaderOptions), new Representer(options));
    }

    @Nullable
    public <T extends BaseConfig> T registerConfig(String modID, ConfigSide side, Supplier<T> configCreator){
        if((Platform.getEnvironment() == Env.SERVER && side == ConfigSide.COMMON) || Platform.getEnvironment() == Env.CLIENT){
            File sideConfig = Paths.get(this.configFolder.toString(), modID + "-" + side.name().toLowerCase(Locale.ENGLISH) + ".yml").toFile();
            T config = configCreator.get();
            if(sideConfig.exists()){
                load(config, sideConfig);
            } else {
                save(config, sideConfig);
            }
            ResourceLocation location = new ResourceLocation(modID, side.name().toLowerCase(Locale.ENGLISH));
            registered_configs.put(location, config);
            return config;
        }
        return null;
    }

    public void saveConfig(ConfigSide configSide){
        ResourceLocation resourceLocation = new ResourceLocation(Theatrical.MOD_ID, configSide.name().toLowerCase(Locale.ENGLISH));
        File sideConfig = Paths.get(this.configFolder.toString(), Theatrical.MOD_ID + "-" + configSide.name().toLowerCase(Locale.ENGLISH) + ".yml").toFile();
        save(registered_configs.get(resourceLocation), sideConfig);
    }

    private <T> void save(T config, File output){
        Map<String, Object> yamlMap = new HashMap<>();
        magicWrite(config, yamlMap);
        try {
            yaml.dump(yamlMap, new FileWriter(output));
        } catch(Exception e) {
            Theatrical.LOGGER.atError().setCause(e)
                .setMessage("Error saving config {} to {}")
                .addArgument(() -> config.getClass().getSimpleName())
                .addArgument(output)
                .log();
        }
    }

    private <T> void load(T config, File input){
        try {
            Map<String, Object> parsedYaml = yaml.load(new FileReader(input.getPath()));
            magicLoad(config, parsedYaml);
        } catch (Exception e) {
            Theatrical.LOGGER.atError().setCause(e)
                .setMessage("Error loading config {} from {}")
                .addArgument(() -> config.getClass().getSimpleName())
                .addArgument(input)
                .log();
        }
    }

    private <T> void magicLoad(T config, Map<String, Object> inputMap){
        Field[] fields = config.getClass().getFields();
        Set<String> incomingStrings = inputMap.keySet();
        for (Field f : fields) {
            if(f.isAnnotationPresent(TheatricalConfigItem.class)) {
                TheatricalConfigItem annotation = f.getAnnotation(TheatricalConfigItem.class);
                String fieldName = annotation.name().length > 0 ? annotation.name()[0] : f.getName();
                Class<?> fieldType = annotation.type().length > 0 ? annotation.type()[0] : f.getType();
                if(!incomingStrings.contains(fieldName)){
                    break;
                }
                Object value = inputMap.get(fieldName);
                if(annotation.maxValue().length > 0 || annotation.minValue().length > 0){
                    if(fieldType.isPrimitive() && value instanceof Number number){
                        if(annotation.maxValue().length > 0 && number.doubleValue() > Double.parseDouble(annotation.maxValue()[0])){
                            Theatrical.LOGGER.atError()
                                .setMessage("Config '{}' value '{}' is larger than the max '{}'")
                                .addArgument(fieldName)
                                .addArgument(number)
                                .addArgument(() -> annotation.maxValue()[0])
                                .log();
                            break;
                        }
                        if(annotation.minValue().length > 0 && number.doubleValue() < Double.parseDouble(annotation.minValue()[0])){
                            Theatrical.LOGGER.atError()
                                .setMessage("Config '{}' value '{}' is smaller than the min '{}'")
                                .addArgument(fieldName)
                                .addArgument(number)
                                .addArgument(() -> annotation.minValue()[0])
                                .log();
                            break;
                        }
                    }
                }
                try {
                    f.set(config, value);
                } catch (IllegalAccessException e) {
                    Theatrical.LOGGER.atError().setCause(e)
                        .setMessage("Error updating config field '{}' with value '{}'")
                        .addArgument(fieldName)
                        .addArgument(value)
                        .log();
                }
            }
        }
    }

    private <T> void magicWrite(T config, Map<String, Object> outputMap){
        Field[] fields = config.getClass().getFields();
        for (Field f : fields) {
            if(f.isAnnotationPresent(TheatricalConfigItem.class)) {
                TheatricalConfigItem annotation = f.getAnnotation(TheatricalConfigItem.class);
                String fieldName = annotation.name().length > 0 ? annotation.name()[0] : f.getName();
                try {
                    outputMap.put(fieldName, f.get(config));
                } catch (IllegalAccessException e) {
                    Theatrical.LOGGER.atError().setCause(e)
                        .setMessage("Error getting config field '{}'")
                        .addArgument(fieldName)
                        .log();
                }
            }
        }
    }

}
