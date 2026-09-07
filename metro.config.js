const { getDefaultConfig } = require("expo/metro-config");
const { withNativeWind } = require("nativewind/metro");

const config = getDefaultConfig(__dirname);

config.resolver.assetExts.push("wasm");

config.resolver.sourceExts.push("sql");

config.transformer.asyncRequireModulePath = require.resolve(
  "metro-runtime/src/modules/asyncRequire",
);

module.exports = withNativeWind(config, { input: "./src/app/global.css" });
