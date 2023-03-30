The file `wwwroot/config.json` in TerriaMap contains client-side configuration parameters. See [this file for an example](https://github.com/TerriaJS/TerriaMap/blob/main/wwwroot/config.json).

It has following structure:

|Name|Required|Type|Default|Description|
|----|--------|----|-------|-----------|
|[initializationUrls](#intializationurls)|yes|**string[]**|[]|The list of initialization files which define the catalog content, for more details check [below](#intializationurls).|
|[v7initializationUrls](#v7initializationurls)|yes|**string[]**|[]|The list of v7 initialization files &mdash; these will be converted to v8 on the fly using [`catalog-converter`](https://github.com/TerriaJS/catalog-converter). For more details check [below](#v7initializationUrls).|
|parameters|yes|**[Parameters](#parameters)**||TerriaJS configuration options|

**Example**
```json5
{
    "initializationUrls" : [
        "myinitfile",
        "anotherinitfile"
    ],
    "parameters": {
        "bingMapsKey": "...",
        ...
    }
}
```

## intializationUrls

Each string in the array specifies a single [initialization file](initialization-files.md) (catalog) to be loaded by TerriaJS.  The init files are loaded in the order they're specified.

If a string ends with `.json`, it is assumed to be a complete relative or absolute URL to an init file.  The file may be on an entirely separate web server, but in that case it must be accessible for [Cross-Origin Resource Sharing (CORS)](../connecting-to-data/cross-origin-resource-sharing.md).  It may also be generated by a service rather than being a simple static file.  If the URL is relative, it is relative to the config file.

If the string does not end with `.json`, such as `"foo"`, it refers to an init file on the same web server at `init/foo.json`.  In a TerriaMap directory on your computer, it can be found at `wwwroot/init/foo.json`.

### v7initializationUrls

It is also possible to add version 7 init files &mdash; these will be converted on-the-fly in `terriajs` when a map is loaded. See [`catalog-converter`](https://github.com/TerriaJS/catalog-converter) repo for more information.

## Parameters

**The best reference for now is [`interface ConfigParameters`](https://github.com/TerriaJS/terriajs/blob/main/lib/Models/Terria.ts#L101) (you may have to search for `interface ConfigParameters` on that page to find it if future code changes change line numbers).** 

Specifies various options for configuring TerriaJS:

|Name|Required|Type|Default|Description|
|----|--------|----|-------|-----------|
|`appName`|no|**string**|`"TerriaJS App"`|TerriaJS uses this name whenever it needs to display the name of the application.|
|`supportEmail`|no|**string**|`"info@terria.io"`|The email address shown when things go wrong.|
|`defaultMaximumShownFeatureInfos`|no|**number**|`100`|The maximum number of "feature info" boxes that can be displayed when clicking a point.|
|`regionMappingDefinitionsUrl`|yes|**string**|`"build/TerriaJS/data/regionMapping.json"`|URL of the JSON file that defines region mapping for CSV files. This option only needs to be changed in unusual deployments. It has to be changed if deploying as static site, for instance.|
|`catalogIndexUrl`|no|**string**||URL of the JSON file that contains index of catalog. See [CatalogIndex](#catalogindex)|
|`conversionServiceBaseUrl`|no|**string**|`"convert/"`|URL of OGR2OGR conversion service (part of TerriaJS-Server). This option only needs to be changed in unusual deployments. It has to be changed if deploying as static site, for instance.|
|`proj4ServiceBaseUrl`|no|**string**|`"proj4def/"`|URL of Proj4 projection lookup service (part of TerriaJS-Server). This option only needs to be changed in unusual deployments. It has to be changed if deploying as static site, for instance.|
|`corsProxyBaseUrl`|no|**string**|`"proxy/"`|URL of CORS proxy service (part of TerriaJS-Server). This option only needs to be changed in unusual deployments. It has to be changed if deploying as static site, for instance.|
|`proxyableDomainsUrl`|no|**string**|`"proxyabledomains/"`|Deprecated, will be determined from serverconfig.|
|`serverConfigUrl`|no|**string**|`"serverconfig/"`|
|`shareUrl`|no|**string**|`"share"`|
|`feedbackUrl`|no|**string**||URL of the service used to send feedback.  If not specified, the "Give Feedback" button will not appear.|
|`initFragmentPaths`|yes|**string[]**|`["init/"]`|An array of base paths to use to try to use to resolve init fragments in the URL.  For example, if this property is `[ "init/", "http://example.com/init/"]`, then a URL with `#test` will first try to load `init/test.json` and, if that fails, next try to load `http://example.com/init/test.json`.|
|`storyEnabled`|yes|**boolean**|`true`|Whether the story is enabled. If false story function button won't be available.|
|`interceptBrowserPrint`|no|**boolean**|`true`|True (the default) to intercept the browser's print feature and use a custom one accessible through the Share panel.|
|`tabbedCatalog`|no|**boolean**|`false`|True to create a separate explorer panel tab for each top-level catalog group to list its items in.|
|`useCesiumIonTerrain`|no|**boolean**|`true`|True to use Cesium World Terrain from Cesium ion. False to use terrain from the URL specified with the `"cesiumTerrainUrl"` property. If this property is false and `"cesiumTerrainUrl"` is not specified, the 3D view will use a smooth ellipsoid instead of a terrain surface. Defaults to true.|
|`cesiumTerrainUrl`|no|**string**|undefined|The URL to use for Cesium terrain in the 3D Terrain viewer, in quantized mesh format. This property is ignored if "useCesiumIonTerrain" is set to true, or if `cesiumTerrainAssetId` is present.|
|`cesiumTerrainAssetId`|no|**number**|undefined|The Cesium Ion Asset ID to use for Cesium terrain in the 3D Terrain viewer. `cesiumIonAccessToken` will be used to authenticate. This property is ignored if "useCesiumIonTerrain" is set to true.|
|`cesiumIonAccessToken`|no|**string**|undefined|The access token to use with Cesium ion. If `"useCesiumIonTerrain"` is true and this property is not specified, the Cesium default Ion key will be used. It is a violation of the Ion terms of use to use the default key in a deployed application.|
|`useCesiumIonBingImagery`|no|**boolean**|`true`|True to use Bing Maps from Cesium ion (Cesium World Imagery). By default, Ion will be used, unless the `bingMapsKey` property is specified, in which case that will be used instead. To disable the Bing Maps layers entirely, set this property to false and set `bingMapsKey` to null.|
|`bingMapsKey`|no|**string**|undefined|A [Bing Maps API key](https://msdn.microsoft.com/en-us/library/ff428642.aspx) used for requesting Bing Maps base maps and using the Bing Maps geocoder for searching. It is your responsibility to request a key and comply with all terms and conditions.|
|`hideTerriaLogo`|no|**boolean**|`false`|
|`brandBarElements`|no|**string[]**|undefined|An array of strings of HTML that fill up the top left logo space (see `brandBarSmallElements` or `displayOneBrand` for small screens).|
|`brandBarSmallElements`|no|**string[]**|undefined|An array of strings of HTML that fill up the top left logo space - used for small screens.|
|`displayOneBrand`|no|**number**|`0`|Index of which `brandBarElements` to show for mobile header. This will only be used if `brandBarSmallElements` is undefined.|
|`disableMyLocation`|no|**boolean**|undefined|True to disable the "Centre map at your current location" button.|
|`disableSplitter`|no|**boolean**|undefined|True to disable the use of the splitter control.|
|`experimentalFeatures`|no|**boolean**|undefined||
|`magdaReferenceHeaders`|no|**[MagdaReferenceHeaders](#magdareferenceheaders)**|undefined|
|`locationSearchBoundingBox`|no|**number**|undefined|
|`googleAnalyticsKey`|no|**string**|undefined|A Google API key for [Google Analytics](https://analytics.google.com).  If specified, TerriaJS will send various events about how it's used to Google Analytics.|
|`errorService`|no|**[ErrorServiceOptions](#errorserviceoptions)**|undefined|Optional configuration for the remote error logging service that Terria should log errors to.|
|`globalDisclaimer`|no|**any**|undefined||
|`showWelcomeMessage`|no|**boolean**|`false`|True to display welcome message on startup.|
|`welcomeMessageVideo`|no|**any**||Video to show in welcome message.|
|`showInAppGuides`|no|**boolean**|`false`|True to display in-app guides.|
|`helpContent`|no|**[HelpContentItem](#helpcontentitem)**|`[]`|The content to be displayed in the help panel.|
|`helpContentTerms`|no|**[Term](#term)**|||
|`languageConfiguration`|no|**[LanguageConfiguration](#languageconfiguration)**|undefined|Language configuration of TerriaJS.|
|`customRequestSchedulerLimits`|no|**[RequestScheduler](https://cesium.com/docs/cesiumjs-ref-doc/RequestScheduler.html#.requestsByServer)**|undefined|Custom concurrent request limits for domains in Cesium's RequestScheduler.|
|`persistViewerMode`|no|**boolean**|`true`|Whether to load persisted viewer mode from local storage.|
|`openAddData`|no|**boolean**|`false`|Whether to open the add data explorer panel on load.|
|feedbackPreamble|no|**string**|feedback.feedbackPreamble|Text showing at the top of feedback form, supports the internationalization using the translation key.|
|feedbackPostamble|no|**string**|feedback.feedbackPostamble|Text showing at the bottom of feedback form, supports the internationalization using the translation key.|
|feedbackMinLength|no|**number**|0|Minimum length of feedback comment.| 
|`theme`|no|**any**|`{}`|An object used to override theme properties - for example `{"logoHeight": "70px"}`.|


### MagdaReferenceHeaders

***

### HelpContentItem
Configuration of items to appear in the search bar

|Name|Required|Type|Default|Description|
|----|--------|----|-------|-----------|
|`itemName`|yes|**string**|undefined|
|`title`|no|**string**|undefined|Title of the help item|
|`videoUrl`|no|**string**|undefined|The video to show on the top of help item.|
|`placeholderImage`|no|**string**|undefined|Placeholder image for the video.|
|`paneMode`|no|**enum["videoAndContent","slider","trainer"]**|`"videoAndContent"`|
|`trainerItems`|no|**[TrainerItem[]](#traineritem)**|undefined|List of the trainer steps|
|`markdownText`|no|**string**|undefined|The content of the help item, can use Markdown syntax.|
|`icon`|no|**string**|undefined|Icon to show next to the itemName.|

#### TrainerItem

|Name|Required|Type|Default|Description|
|----|--------|----|-------|-----------|
|title|yes|**string**||Title of the trainer item.|
|footnote|yes|**string**||Text to show below steps.|
|steps|yes|**StepItem**||List of the steps for this trainer item.|

#### StepItem

|Name|Required|Type|Default|Description|
|----|--------|----|-------|-----------|
|title|yes|**string**||Title of the step.|
|markdownDescription|no|**string**||The content of the step item.|

***

### Term
|Name|Required|Type|Default|Description|
|----|--------|----|-------|-----------|
|term|yes|**string**||Name of the term, content will be attached to it when found in text.|
|content|yes|**string**||Description of the content.|
|aliases|no|**string[]**||Aliases of the term.|

***

### LanguageConfiguration

|Name|Required|Type|Default|Description|
|----|--------|----|-------|-----------|
|enabled|yes|**boolean**|`false`|Controls whether a button to switch the portal's language is provided.|
|debug|yes|**boolean**|`false`|Controls whether debug information regarding translations is logged to the console.|
|react|yes|**ReactOptions**||
|languages|yes|**Object**|`{en: "english"}`|Language abbreviations. Please mind that matching locale files must exist.|
|fallbackLanguage|yes|**string**|`"en"`|Fallback language used if contents are not available in the currently selected language.|
|changeLanguageOnStartWhen|yes|**string[]**|`["querystring", "localStorage", "navigator", "htmlTag"]`|Order of user language detection. See [i18next browser language detection documentation](https://github.com/i18next/i18next-browser-languageDetector) for details.|

***

### ErrorServiceOptions
|Name|Required|Type|Default|Description|
|----|--------|----|-------|-----------|
|provider|yes|**string**|`undefined`|A string identifying the error service provider to use. Currently only `rollbar` is supported.|
|configuration|no|**any**|`undefined`|The configuration object to pass as constructor parameters to the error service provider instance. See the [provider implementation](https://github.com/TerriaJS/terriajs/blob/main/lib/Models/ErrorServiceProviders/) for supported configuration parameters.|

**Example**

```json
{
  "enabled": true,
  "debug": false,
  "react": {
    "useSuspense": false
  },
  "languages": {
    "en": "english",
    "de": "deutsch"
  },
  "fallbackLanguage": "en",
  "changeLanguageOnStartWhen": [
    "querystring",
    "localStorage",
    "navigator",
    "htmlTag"
  ]
}
```

***

### CatalogIndex

If your TerriaMap has many (>50) dynamic groups (groups which need to be loaded - for example CKAN, WMS-group...) it may be worth generating a static catalog index JSON file. This file will contain ID, name and description fields of all catalog items, which can be used to search through the catalog very quickly without needing to load dynamic groups.

The https://github.com/nextapps-de/flexsearch library is used to index and search the catalog index file.

**Note** NodeJS v10 is not supported, please use v12 or v14.

To generate the catalog index:

- `yarn build-tools`
- `node .\build\generateCatalogIndex.js config-url base-url` where
  - `config-url` is URL to client-side-config file
  - `base-url` is URL to terriajs-server (this is used to load `server-config` and to proxy requests)
  - For example `node .\build\generateCatalogIndex.js http://localhost:3001/config.json http://localhost:3001`

- This will output three files
  - `catalog-index.json`
  - `catalog-index-errors.json` with any error messages which occurred while loading catalog members
  - `catalog-index-errors-stack.json` with errors stack
- Set `catalogIndexUrl` config parameter to URL to `catalog-index.json`

This file will have to be re-generated manually every time the catalog structure changes - for example:

- if items are renamed, or moved
- dynamic groups are updated (for example, WMS server publishes new layers)

For more details see [/buildprocess/generateCatalogIndex.ts](/buildprocess/generateCatalogIndex.ts)