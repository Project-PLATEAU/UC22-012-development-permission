import {
  action,
  computed,
  IReactionDisposer,
  observable,
  reaction,
  runInAction
} from "mobx";
import { Ref } from "react";
import defined from "terriajs-cesium/Source/Core/defined";
import CesiumEvent from "terriajs-cesium/Source/Core/Event";
import addedByUser from "../Core/addedByUser";
import {
  Category,
  HelpAction,
  StoryAction,
  LotSearchAction,
  ApplicationCategorySelection,
  GeneralConditionDiagnosis,
  DistrictNameSearchAction,
  KanaDistrictNameSearchAction,
  EnterApplicantInformation,
  UploadApplicationInformation,
  ConfirmApplicationDetails,
  CustomMessage,
  AnswerLogin,
  AnswerContent,
  UserAgreement,
  MapSelection,
  CharacterSelection,
  ApplicationInformationSearch,
  ApplicationDetails,
  AnswerInput,
  ApplicationList
} from "../Core/AnalyticEvents/analyticEvents";
import Result from "../Core/Result";
import triggerResize from "../Core/triggerResize";
import PickedFeatures from "../Map/PickedFeatures";
import CatalogMemberMixin, { getName } from "../ModelMixins/CatalogMemberMixin";
import GroupMixin from "../ModelMixins/GroupMixin";
import MappableMixin from "../ModelMixins/MappableMixin";
import ReferenceMixin from "../ModelMixins/ReferenceMixin";
import CommonStrata from "../Models/Definition/CommonStrata";
import { BaseModel } from "../Models/Definition/Model";
import getAncestors from "../Models/getAncestors";
import Terria from "../Models/Terria";
import { SATELLITE_HELP_PROMPT_KEY } from "../ReactViews/HelpScreens/SatelliteHelpPrompt";
import { animationDuration } from "../ReactViews/StandardUserInterface/StandardUserInterface";
import {
  defaultTourPoints,
  RelativePosition,
  TourPoint
} from "./defaultTourPoints";
import DisclaimerHandler from "./DisclaimerHandler";
import SearchState from "./SearchState";
import ViewerMode, {
  MapViewers,
  setViewerMode
} from "../Models/ViewerMode";
import ImagerySplitDirection from "terriajs-cesium/Source/Scene/ImagerySplitDirection";
import webMapServiceCatalogItem from '../Models/Catalog/Ows/WebMapServiceCatalogItem';
import Config from "../../customconfig.json";

export const DATA_CATALOG_NAME = "data-catalog";
export const USER_DATA_NAME = "my-data";

// check showWorkbenchButton delay and transforms
// export const WORKBENCH_RESIZE_ANIMATION_DURATION = 250;
export const WORKBENCH_RESIZE_ANIMATION_DURATION = 500;

interface ViewStateOptions {
  terria: Terria;
  catalogSearchProvider: any;
  locationSearchProviders: any[];
  errorHandlingProvider?: any;
}

/**
 * Root of a global view model. Presumably this should get nested as more stuff goes into it. Basically this belongs to
 * the root of the UI and then it can choose to pass either the whole thing or parts down as props to its children.
 */

export default class ViewState {
  readonly mobileViewOptions = Object.freeze({
    data: "data",
    preview: "preview",
    nowViewing: "nowViewing",
    locationSearchResults: "locationSearchResults"
  });
  readonly searchState: SearchState;
  readonly terria: Terria;
  readonly relativePosition = RelativePosition;

  @observable private _previewedItem: BaseModel | undefined;
  get previewedItem() {
    return this._previewedItem;
  }
  @observable userDataPreviewedItem: BaseModel | undefined;
  @observable explorerPanelIsVisible: boolean = false;
  @observable activeTabCategory: string = DATA_CATALOG_NAME;
  @observable activeTabIdInCategory: string | undefined = undefined;
  @observable isDraggingDroppingFile: boolean = false;
  @observable mobileView: string | null = null;
  @observable isMapFullScreen: boolean = false;
  @observable myDataIsUploadView: boolean = true;
  @observable mobileMenuVisible: boolean = false;
  @observable explorerPanelAnimating: boolean = false;
  @observable topElement: string = "FeatureInfo";
  @observable lastUploadedFiles: any[] = [];
  @observable storyBuilderShown: boolean = false;

  // Flesh out later
  @observable showHelpMenu: boolean = false;
  @observable showSatelliteGuidance: boolean = false;
  @observable showWelcomeMessage: boolean = false;
  @observable selectedHelpMenuItem: string = "";
  @observable helpPanelExpanded: boolean = false;
  @observable disclaimerSettings: any | undefined = undefined;
  @observable disclaimerVisible: boolean = false;
  @observable videoGuideVisible: string = "";

  @observable trainerBarVisible: boolean = false;
  @observable trainerBarExpanded: boolean = false;
  @observable trainerBarShowingAllSteps: boolean = false;
  @observable selectedTrainerItem: string = "";
  @observable currentTrainerItemIndex: number = 0;
  @observable currentTrainerStepIndex: number = 0;

  @observable printWindow: Window | null = null;

  //↓カスタム機能

  // 地番検索画面表示有無
  @observable showLotNumberSearch: boolean = false;
  // 町丁名一覧画面表示有無
  @observable showDistrictNameSearch: boolean = false;
  // かな検索画面表示有無
  @observable showKanaDistrictNameSearch: boolean = false;
  // 利用者規約画面表示有無 
  @observable showUserAgreement: boolean = false;
  // 申請区分選択画面表示有無 
  @observable showApplicationCategorySelection: boolean = false;
  // 地図選択画面表示有無 
  @observable showMapSelection: boolean = false;
  // 文字選択画面表示有無 
  @observable showCharacterSelection: boolean = false;
  // 概況診断画面表示有無 
  @observable showGeneralConditionDiagnosis: boolean = false;
  // 申請者情報入力画面表示有無 
  @observable showEnterApplicantInformation: boolean = false;
  // 申請ファイル登録画面表示有無 
  @observable showUploadApplicationInformation: boolean = false;
  // 申請内容確認画面表示有無 
  @observable showConfirmApplicationDetails: boolean = false;
  // カスタムメッセージ画面表示有無 
  @observable showCustomMessage: boolean = false;
  // 申請ID/パスワード照合画面表示有無
  @observable showAnswerLogin: boolean = false;
  // 申請内容確認画面表示有無
  @observable showAnswerContent: boolean = false;
  // 申請一覧のサブ画面表示有無
  @observable showApplicationList: boolean = false;
  // 申請情報検索画面表示有無
  @observable showApplicationInformationSearch: boolean = false;
  // 申請情報詳細画面表示有無
  @observable showApplicationDetails: boolean = false;
  // 回答内容入力画面表示有無
  @observable showAnswerInput: boolean = false;

  // 選択済み申請区分
  checkedApplicationCategory:Object = [];
  // 選択済み申請区分ローカル保持用
  checkedApplicationCategoryLocalSave:Object = [];
  // 概況診断結果
  generalConditionDiagnosisResult:Object = {};
  // 一時フォルダ名
  folderName:string = "";
  // 申請者情報
  applicantInformation:Object = [];
  // 申請ファイル
  applicationFile:Object = {};
  // 回答内容確認
  answerContent:Object = {};
  // 申請一覧
  applicationList:Object = {};
  // 申請地情報
  @observable applicationPlace:any[] = [];
  // 申請地情報temp用
  applicationPlaceTemp:any[] = [];
  //地番検索結果チェックボックス選択状態
  @observable selectLotSearchResult:any[] = [];
  //地番検索パネル表示フラグ true:地番検索単体 false:申請地選択に埋め込み
  islotNumberSearchViewIndependent: boolean = true;
  //地番検索パネル位置補正値(top)
  lotNumberSearchPosDiffTop:number = 0;
  //地番検索パネル位置補正値(left)
  lotNumberSearchPosDiffLeft:number = 0;
  //行政側か否か
  isUserGoverment: boolean = false;
  //検索対象の町丁名
  @observable searchDistrictName:any = null;
  //検索対象の町丁ID
  @observable searchDistrictId:any = null;
  //検索結果
  @observable searchResult:any = null;
  //3dModeキー
  map3dModeKey: keyof typeof MapViewers = "3d";
  //申請ID
  applicationInformationSearchForApplicationId:string = "0";
  //カスタムメッセージ タイトル
  customMessageTitle:string = "";
  //カスタムメッセージ 内容
  customMessageContent:string = "";
  //地番検索結果一覧保持用
  SearchRsultContent:any="";
  //かな検索結果一覧保持用
  KanaDistrictNameSearch:any="";
  //町丁名一覧結果保持用
  DistrictNameSearch:any="";

  //↑カスタム機能
  @action
  setSelectedTrainerItem(trainerItem: string) {
    this.selectedTrainerItem = trainerItem;
  }
  @action
  setTrainerBarVisible(bool: boolean) {
    this.trainerBarVisible = bool;
  }
  @action
  setTrainerBarShowingAllSteps(bool: boolean) {
    this.trainerBarShowingAllSteps = bool;
  }
  @action
  setTrainerBarExpanded(bool: boolean) {
    this.trainerBarExpanded = bool;
    // if collapsing trainer bar, also hide steps
    if (!bool) {
      this.trainerBarShowingAllSteps = bool;
    }
  }
  @action
  setCurrentTrainerItemIndex(index: number) {
    this.currentTrainerItemIndex = index;
    this.currentTrainerStepIndex = 0;
  }
  @action
  setCurrentTrainerStepIndex(index: number) {
    this.currentTrainerStepIndex = index;
  }

  /**
   * Bottom dock state & action
   */
  @observable bottomDockHeight: number = 0;
  @action
  setBottomDockHeight(height: number) {
    if (this.bottomDockHeight !== height) {
      this.bottomDockHeight = height;
    }
  }

  @observable workbenchWithOpenControls: string | undefined = undefined;

  errorProvider: any | null = null;

  // default value is null, because user has not made decision to show or
  // not show story
  // will be explicitly set to false when user 1. dismiss story
  // notification or 2. close a story
  @observable storyShown: boolean | null = null;

  @observable currentStoryId: number = 0;
  @observable featurePrompts: any[] = [];

  /**
   * we need a layering system for touring the app, but also a way for it to be
   * chopped and changed from a terriamap
   *
   * this will be slightly different to the help sequences that were done in
   * the past, but may evolve to become a "sequence" (where the UI gets
   * programatically toggled to delve deeper into the app, e.g. show the user
   * how to add data via the data catalog window)
   *
   * rough points
   * - "all guide points visible"
   * -
   *

   * draft structure(?):
   *
   * maybe each "guide" item will have
   * {
   *  ref: (react ref object)
   *  dotOffset: (which way the dot and guide should be positioned relative to the ref component)
   *  content: (component, more flexibility than a string)
   * ...?
   * }
   * and guide props?
   * {
   *  enabled: parent component to decide this based on active index
   * ...?
   * }
   *  */

  @observable tourPoints: TourPoint[] = defaultTourPoints;
  @observable showTour: boolean = false;
  @observable appRefs: Map<string, Ref<HTMLElement>> = new Map();
  @observable currentTourIndex: number = -1;
  @observable showCollapsedNavigation: boolean = false;

  get tourPointsWithValidRefs() {
    // should viewstate.ts reach into document? seems unavoidable if we want
    // this to be the true source of tourPoints.
    // update: well it turns out you can be smarter about it and actually
    // properly clean up your refs - so we'll leave that up to the UI to
    // provide valid refs
    return this.tourPoints
      .sort((a, b) => {
        return a.priority - b.priority;
      })
      .filter(
        tourPoint => (<any>this.appRefs).get(tourPoint.appRefName)?.current
      );
  }
  @action
  setTourIndex(index: number) {
    this.currentTourIndex = index;
  }
  @action
  setShowTour(bool: boolean) {
    this.showTour = bool;
    // If we're enabling the tour, make sure the trainer is collapsed
    if (bool) {
      this.setTrainerBarExpanded(false);
    }
  }
  @action
  closeTour() {
    this.currentTourIndex = -1;
    this.showTour = false;
  }
  @action
  previousTourPoint() {
    const currentIndex = this.currentTourIndex;
    if (currentIndex !== 0) {
      this.currentTourIndex = currentIndex - 1;
    }
  }
  @action
  nextTourPoint() {
    const totalTourPoints = this.tourPointsWithValidRefs.length;
    const currentIndex = this.currentTourIndex;
    if (currentIndex >= totalTourPoints - 1) {
      this.closeTour();
    } else {
      this.currentTourIndex = currentIndex + 1;
    }
  }
  @action
  closeCollapsedNavigation() {
    this.showCollapsedNavigation = false;
  }

  @action
  updateAppRef(refName: string, ref: Ref<HTMLElement>) {
    if (!this.appRefs.get(refName) || this.appRefs.get(refName) !== ref) {
      this.appRefs.set(refName, ref);
    }
  }
  @action
  deleteAppRef(refName: string) {
    this.appRefs.delete(refName);
  }

  /**
   * Gets or sets a value indicating whether the small screen (mobile) user interface should be used.
   * @type {Boolean}
   */
  @observable useSmallScreenInterface: boolean = false;

  /**
   * Gets or sets a value indicating whether the feature info panel is visible.
   * @type {Boolean}
   */
  @observable featureInfoPanelIsVisible: boolean = false;

  /**
   * Gets or sets a value indicating whether the feature info panel is collapsed.
   * When it's collapsed, only the title bar is visible.
   * @type {Boolean}
   */
  @observable featureInfoPanelIsCollapsed: boolean = false;

  /**
   * True if this is (or will be) the first time the user has added data to the map.
   * @type {Boolean}
   */
  @observable firstTimeAddingData: boolean = true;

  /**
   * Gets or sets a value indicating whether the feedback form is visible.
   * @type {Boolean}
   */
  @observable feedbackFormIsVisible: boolean = false;

  /**
   * Gets or sets a value indicating whether the catalog's model share panel
   * is currently visible.
   */
  @observable shareModelIsVisible: boolean = false;

  /**
   * The currently open tool
   */
  @observable currentTool?: Tool;

  @observable panel: React.ReactNode;

  private _pickedFeaturesSubscription: IReactionDisposer;
  private _disclaimerVisibleSubscription: IReactionDisposer;
  private _isMapFullScreenSubscription: IReactionDisposer;
  private _showStoriesSubscription: IReactionDisposer;
  private _mobileMenuSubscription: IReactionDisposer;
  private _storyPromptSubscription: IReactionDisposer;
  private _previewedItemIdSubscription: IReactionDisposer;
  private _workbenchHasTimeWMSSubscription: IReactionDisposer;
  private _storyBeforeUnloadSubscription: IReactionDisposer;
  private _disclaimerHandler: DisclaimerHandler;

  constructor(options: ViewStateOptions) {
    const terria = options.terria;
    this.searchState = new SearchState({
      terria: terria,
      catalogSearchProvider: options.catalogSearchProvider,
      locationSearchProviders: options.locationSearchProviders
    });

    this.errorProvider = options.errorHandlingProvider
      ? options.errorHandlingProvider
      : null;
    this.terria = terria;

    // When features are picked, show the feature info panel.
    this._pickedFeaturesSubscription = reaction(
      () => this.terria.pickedFeatures,
      (pickedFeatures: PickedFeatures | undefined) => {
        if (defined(pickedFeatures)) {
          this.featureInfoPanelIsVisible = true;
          this.featureInfoPanelIsCollapsed = false;
        } else {
          this.featureInfoPanelIsVisible = false;
        }
      }
    );
    // When disclaimer is shown, ensure fullscreen
    // unsure about this behaviour because it nudges the user off center
    // of the original camera set from config once they acknowdge
    this._disclaimerVisibleSubscription = reaction(
      () => this.disclaimerVisible,
      disclaimerVisible => {
        if (disclaimerVisible) {
          this.isMapFullScreen = true;
        } else if (!disclaimerVisible && this.isMapFullScreen) {
          this.isMapFullScreen = false;
        }
      }
    );

    this._isMapFullScreenSubscription = reaction(
      () =>
        terria.userProperties.get("hideWorkbench") === "1" ||
        terria.userProperties.get("hideExplorerPanel") === "1",
      (isMapFullScreen: boolean) => {
        this.isMapFullScreen = isMapFullScreen;

        // if /#hideWorkbench=1 exists in url onload, show stories directly
        // any show/hide workbench will not automatically show story
        if (!defined(this.storyShown)) {
          // why only checkk config params here? because terria.stories are not
          // set at the moment, and that property will be checked in rendering
          // Here are all are checking are: is terria story enabled in this app?
          // if so we should show it when app first laod, if workbench is hiddne
          this.storyShown = terria.configParameters.storyEnabled;
        }
      }
    );

    this._showStoriesSubscription = reaction(
      () => Boolean(terria.userProperties.get("playStory")),
      (playStory: boolean) => {
        this.storyShown = terria.configParameters.storyEnabled && playStory;
      }
    );

    this._mobileMenuSubscription = reaction(
      () => this.mobileMenuVisible,
      (mobileMenuVisible: boolean) => {
        if (mobileMenuVisible) {
          this.explorerPanelIsVisible = false;
          this.switchMobileView(null);
        }
      }
    );

    this._disclaimerHandler = new DisclaimerHandler(terria, this);

    this._workbenchHasTimeWMSSubscription = reaction(
      () => this.terria.workbench.hasTimeWMS,
      (hasTimeWMS: boolean) => {
        if (
          this.terria.configParameters.showInAppGuides &&
          hasTimeWMS === true &&
          // // only show it once
          !this.terria.getLocalProperty(`${SATELLITE_HELP_PROMPT_KEY}Prompted`)
        ) {
          this.setShowSatelliteGuidance(true);
          this.toggleFeaturePrompt(SATELLITE_HELP_PROMPT_KEY, true, true);
        }
      }
    );

    this._storyPromptSubscription = reaction(
      () => this.storyShown,
      (storyShown: boolean | null) => {
        if (storyShown === false) {
          // only show it once
          if (!this.terria.getLocalProperty("storyPrompted")) {
            this.toggleFeaturePrompt("story", true, false);
          }
        }
      }
    );

    this._previewedItemIdSubscription = reaction(
      () => this.terria.previewedItemId,
      (previewedItemId: string | undefined) => {
        if (previewedItemId === undefined) {
          return;
        }

        const model = this.terria.getModelById(BaseModel, previewedItemId);
        if (model !== undefined) {
          this.viewCatalogMember(model);
        }
      }
    );

    const handleWindowClose = (e: BeforeUnloadEvent) => {
      // Cancel the event
      e.preventDefault(); // If you prevent default behavior in Mozilla Firefox prompt will always be shown
      // Chrome requires returnValue to be set
      e.returnValue = "";
    };

    this._storyBeforeUnloadSubscription = reaction(
      () => this.terria.stories.length > 0,
      hasScenes => {
        if (hasScenes) {
          window.addEventListener("beforeunload", handleWindowClose);
        } else {
          window.removeEventListener("beforeunload", handleWindowClose);
        }
      }
    );
  }

  dispose() {
    this._pickedFeaturesSubscription();
    this._disclaimerVisibleSubscription();
    this._mobileMenuSubscription();
    this._isMapFullScreenSubscription();
    this._showStoriesSubscription();
    this._storyPromptSubscription();
    this._previewedItemIdSubscription();
    this._workbenchHasTimeWMSSubscription();
    this._disclaimerHandler.dispose();
    this.searchState.dispose();
  }

  @action
  triggerResizeEvent() {
    triggerResize();
  }

  @action
  setIsMapFullScreen(
    bool: boolean,
    animationDuration = WORKBENCH_RESIZE_ANIMATION_DURATION
  ) {
    this.isMapFullScreen = bool;
    // Allow any animations to finish, then trigger a resize.

    // (wing): much better to do by listening for transitionend, but will leave
    // this as is until that's in place
    setTimeout(function() {
      // should we do this here in viewstate? it pulls in browser dependent things,
      // and (defensively) calls it.
      // but only way to ensure we trigger this resize, by standardising fullscreen
      // toggle through an action.
      triggerResize();
    }, animationDuration);
  }

  @action
  toggleStoryBuilder() {
    this.storyBuilderShown = !this.storyBuilderShown;
  }

  @action
  setTopElement(key: string) {
    this.topElement = key;
  }

  @action
  openAddData() {
    this.explorerPanelIsVisible = true;
    this.activeTabCategory = DATA_CATALOG_NAME;
    this.switchMobileView(this.mobileViewOptions.data);
  }

  @action
  openUserData() {
    this.explorerPanelIsVisible = true;
    this.activeTabCategory = USER_DATA_NAME;
  }

  @action
  closeCatalog() {
    this.explorerPanelIsVisible = false;
    this.switchMobileView(null);
    this.clearPreviewedItem();
  }

  @action
  searchInCatalog(query: string) {
    this.openAddData();
    this.searchState.catalogSearchText = query;
    this.searchState.searchCatalog();
  }

  @action
  clearPreviewedItem() {
    this.userDataPreviewedItem = undefined;
    this._previewedItem = undefined;
  }

  /**
   * Views a model in the catalog. If model is a
   *
   * - `Reference` - it will be dereferenced first.
   * - `CatalogMember` - `loadMetadata` will be called
   * - `Group` - its `isOpen` trait will be set according to the value of the `isOpen` parameter in the `stratum` indicated.
   *   - If after doing this the group is open, its members will be loaded with a call to `loadMembers`.
   * - `Mappable` - `loadMapItems` will be called
   *
   * Then (if no errors have occurred) it will open the catalog.
   * Note - `previewItem` is set at the start of the function, regardless of errors.
   *
   * @param item The model to view in catalog.
   * @param [isOpen=true] True if the group should be opened. False if it should be closed.
   * @param stratum The stratum in which to mark the group opened or closed.
   * @param openAddData True if data catalog window should be opened.
   */
  async viewCatalogMember(
    item: BaseModel,
    isOpen: boolean = true,
    stratum: string = CommonStrata.user,
    openAddData = true
  ): Promise<Result<void>> {
    try {
      // Get referenced target first.
      if (ReferenceMixin.isMixedInto(item)) {
        (await item.loadReference()).throwIfError();
        if (item.target) {
          return this.viewCatalogMember(item.target);
        } else {
          return Result.error(`Could not view catalog member ${getName(item)}`);
        }
      }
      const theItem: BaseModel =
        ReferenceMixin.isMixedInto(item) && item.target ? item.target : item;

      // Set preview item
      runInAction(() => (this._previewedItem = theItem));

      // Open "Add Data"
      if (openAddData) {
        if (addedByUser(theItem)) {
          runInAction(() => (this.userDataPreviewedItem = theItem));

          this.openUserData();
        } else {
          runInAction(() => {
            this.openAddData();
            if (this.terria.configParameters.tabbedCatalog) {
              const parentGroups = getAncestors(item);
              if (parentGroups.length > 0) {
                // Go to specific tab
                this.activeTabIdInCategory = parentGroups[0].uniqueId;
              }
            }
          });
        }

        // mobile switch to now vewing if not viewing a group
        if (!GroupMixin.isMixedInto(theItem)) {
          this.switchMobileView(this.mobileViewOptions.preview);
        }
      }

      if (GroupMixin.isMixedInto(theItem)) {
        theItem.setTrait(stratum, "isOpen", isOpen);
        if (theItem.isOpen) {
          (await theItem.loadMembers()).throwIfError();
        }
      } else if (MappableMixin.isMixedInto(theItem))
        (await theItem.loadMapItems()).throwIfError();
      else if (CatalogMemberMixin.isMixedInto(theItem))
        (await theItem.loadMetadata()).throwIfError();
    } catch (e) {
      return Result.error(e, `Could not view catalog member ${getName(item)}`);
    }
    return Result.none();
  }

  @action
  switchMobileView(viewName: string | null) {
    this.mobileView = viewName;
  }

  @action
  showHelpPanel() {
    this.terria.analytics?.logEvent(Category.help, HelpAction.panelOpened);
    this.showHelpMenu = true;
    this.helpPanelExpanded = false;
    this.selectedHelpMenuItem = "";
    this.setTopElement("HelpPanel");
  }

  @action
  selectHelpMenuItem(key: string) {
    this.selectedHelpMenuItem = key;
    this.helpPanelExpanded = true;
  }

  @action
  hideHelpPanel() {
    this.showHelpMenu = false;
  }

  @action
  changeSearchState(newText: string) {
    this.searchState.catalogSearchText = newText;
  }

  @action
  setDisclaimerVisible(bool: boolean) {
    this.disclaimerVisible = bool;
  }

  @action
  hideDisclaimer() {
    this.setDisclaimerVisible(false);
  }

  @action
  setShowSatelliteGuidance(showSatelliteGuidance: boolean) {
    this.showSatelliteGuidance = showSatelliteGuidance;
  }

  @action
  setShowWelcomeMessage(welcomeMessageShown: boolean) {
    this.showWelcomeMessage = welcomeMessageShown;
  }

  @action
  setVideoGuideVisible(videoName: string) {
    this.videoGuideVisible = videoName;
  }

  /**
   * Removes references of a model from viewState
   */
  @action
  removeModelReferences(model: BaseModel) {
    if (this._previewedItem === model) this._previewedItem = undefined;
    if (this.userDataPreviewedItem === model)
      this.userDataPreviewedItem = undefined;
  }

  @action
  toggleFeaturePrompt(
    feature: string,
    state: boolean,
    persistent: boolean = false
  ) {
    const featureIndexInPrompts = this.featurePrompts.indexOf(feature);
    if (
      state &&
      featureIndexInPrompts < 0 &&
      !this.terria.getLocalProperty(`${feature}Prompted`)
    ) {
      this.featurePrompts.push(feature);
    } else if (!state && featureIndexInPrompts >= 0) {
      this.featurePrompts.splice(featureIndexInPrompts, 1);
    }
    if (persistent) {
      this.terria.setLocalProperty(`${feature}Prompted`, true);
    }
  }

  viewingUserData() {
    return this.activeTabCategory === USER_DATA_NAME;
  }

  afterTerriaStarted() {
    if (this.terria.configParameters.openAddData) {
      this.openAddData();
    }
  }

  @action
  openTool(tool: Tool) {
    this.currentTool = tool;
  }

  @action
  closeTool() {
    this.currentTool = undefined;
  }

  @action setPrintWindow(window: Window | null) {
    if (this.printWindow) {
      this.printWindow.close();
    }
    this.printWindow = window;
  }

  @action
  toggleMobileMenu() {
    this.setTopElement("mobileMenu");
    this.mobileMenuVisible = !this.mobileMenuVisible;
  }

  @action
  runStories() {
    this.storyBuilderShown = false;
    this.storyShown = true;

    setTimeout(function() {
      triggerResize();
    }, animationDuration || 1);

    this.terria.currentViewer.notifyRepaintRequired();

    this.terria.analytics?.logEvent(Category.story, StoryAction.runStory);
  }

  /**
   * ↓カスタム機能
   */
  //------------UI013地番検索------------

  /**
   * 地番検索画面を表示
   */
  @action
  showLotNumberSearchView() {
    this.terria.analytics?.logEvent(Category.LotNumberSearch, LotSearchAction.panelOpened);
    this.showLotNumberSearch = true;
    //this.islotNumberSearchViewIndependent = false;
    this.lotNumberSearchPosDiffTop = 0;
    this.lotNumberSearchPosDiffLeft = 0;
    this.selectLotSearchResult = [];
    this.islotNumberSearchViewIndependent = true;
    this.setTopElement("LotNumberSearch");
  }

  /**
   * 町丁名一覧画面を表示
   */
  @action showDistrictNameSearchView() {
    this.terria.analytics?.logEvent(Category.DistrictNameSearch, DistrictNameSearchAction.panelOpened);
    this.showDistrictNameSearch = true;
    this.setTopElement("DistrictNameSearch");
  }

  /**
   * かな検索画面を表示
   */
  @action showKanaDistrictNameSearchView() {
    this.terria.analytics?.logEvent(Category.KanaDistrictNameSearch, KanaDistrictNameSearchAction.panelOpened);
    this.showKanaDistrictNameSearch = true;
    this.setTopElement("KanaDistrictNameSearch");
  }

  /**
   * 地番の選択状態(チェックボックス)を変更
   */
  @action switchLotNumberSelect(lotObj: any) {
    const index = this.selectLotSearchResult.findIndex(obj => 
      obj.chibanId == lotObj.chibanId
    );
    (index != -1)? this.selectLotSearchResult.splice(index, 1): this.selectLotSearchResult.push(lotObj);
  }

  /**
   * 地番の選択状態(チェックボックス)を削除
   */
  @action removeLosNumberSelect() {
    this.selectLotSearchResult = [];
  }

  /**
   * 地番検索画面を閉じる
   */
  @action
  hideLotNumberSearchView() {
    this.searchDistrictName = null;
    this.searchDistrictId = null;
    this.searchDistrictName = null;
    this.showLotNumberSearch = false;
    this.showDistrictNameSearch = false;
    this.showKanaDistrictNameSearch = false;
  }

  /**
   * 町丁名一覧画面を閉じる
   */
  @action
  hideDistrictNameSearchView() {
    this.showDistrictNameSearch = false;
  }

  /**
   * かな検索画面を閉じる
   */
  @action
  hideKanaDistrictNameSearchView() {
    this.showKanaDistrictNameSearch = false;
  }

  /**
   * 選択した町丁名をセットする
   * @param {string} 町丁ID
   * @param {string} 町丁名
   */
  @action
  setDistrictName(districtId: string, districtName:string) {
    this.searchDistrictId = districtId;
    this.searchDistrictName = districtName;
  }

  /**
   * 町丁名をセットする
   * @param {string} 町丁名
   */
  @action
  setSearchDistrictName(districtName:string) {
    this.searchDistrictName = districtName;
  }

  /**
   * 検索結果一覧を保持する
   * @param {any} 検索結果一覧
   */
  @action
  setSearchRsultContent(SearchRsultContent:any){
    this.SearchRsultContent = SearchRsultContent;
  }

  /**
   * 町丁名一覧結果を保持する
   * @param {any} 町丁名一覧結果
   */
  @action
  setDistrictNameSearch(DistrictNameSearch:any){
    this.DistrictNameSearch = DistrictNameSearch;
  }

  /**
   * かな検索結果一覧を保持する
   * @param {any} かな検索結果一覧
   */
  @action
  setKanaDistrictNameSearch(KanaDistrictNameSearch:any){
    this.KanaDistrictNameSearch = KanaDistrictNameSearch;
  }

  //------------共通------------

  /**
   * viewermodeを3Dに切り替える
   */
  @action
  set3dMode() {
    const mainViewer = this.terria.mainViewer;
    this.terria.terrainSplitDirection = ImagerySplitDirection.NONE;
    setViewerMode(this.map3dModeKey, mainViewer);
    this.terria.setLocalProperty("viewermode", this.map3dModeKey);
    this.terria.currentViewer.notifyRepaintRequired();
  }

  /**
   * カスタムメッセージ画面を表示する
   */
  @action
  hideCustomMessageView() {
    this.customMessageTitle = "";
    this.customMessageContent = "";
    this.showCustomMessage = false;
  }

  /**
   * カスタムメッセージをセットする
   * @param {string} タイトル
   * @param {string} 内容
   */
  @action
  setCustomMessage(title:string,content:string) {
    this.customMessageTitle = title;
    this.customMessageContent = content;
  }

  /**
   * 申請地レイヤーを表示する
   */
  @action
  showApplicationAreaLayer(){
    this.terria.setClickMode("2");
    if(this.terria.authorityJudgment()){
      const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForApplying, this.terria);
      const wmsUrl = Config.config.geoserverUrl;
      const items = this.terria.workbench.items;
      for (const aItem of items) {
          if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplying) {
              this.terria.workbench.remove(aItem);
          }
      }
      item.setTrait(CommonStrata.definition, "url", wmsUrl);
      item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForApplying);
      item.setTrait(
          CommonStrata.user,
          "layers",
          Config.layer.lotnumberSearchLayerNameForApplying);
      item.setTrait(CommonStrata.user,
          "parameters",
          {});
      item.loadMapItems();
      this.terria.workbench.add(item); 

    }
  }
  //------------UI001利用者規約------------
  
  /**
   * 利用者規約画面を表示
   */
  @action
  showUserAgreementView(){
    if(!this.terria.authorityJudgment()){
      this.terria.analytics?.logEvent(Category.UserAgreement, UserAgreement.panelOpened);
      this.showUserAgreement = true;
      this.setTopElement("UserAgreement");
    }
  }

  /**
   * 利用者規約画面を閉じる
   */
  @action
  hideUserAgreementView(){
    this.showUserAgreement = false;
  }

  //------------UI003申請区分選択------------

  /**
   * 申請区分選択画面を表示
   */
  @action
  showApplicationCategorySelectionView() {
    this.terria.analytics?.logEvent(Category.ApplicationCategorySelection, ApplicationCategorySelection.panelOpened);
    this.showApplicationCategorySelection = true;
    this.setTopElement("ApplicationCategorySelection");
  }
  /**
   * 申請区分選択画面を閉じる
   */
  @action
  hideApplicationCategorySelectionView() {
    this.initInputApplication();
    this.showApplicationCategorySelection = false;
  }
  /**
   * 申請区分選択画面へ戻る
   */
  @action
  backApplicationCategorySelectionView() {
    this.showCharacterSelection = false;
    this.showMapSelection = false;
    this.terria.setClickMode("");
    this.terria.analytics?.logEvent(Category.ApplicationCategorySelection, ApplicationCategorySelection.panelOpened);
    this.showApplicationCategorySelection = true;
    this.setTopElement("ApplicationCategorySelection");
  }

  //------------UI004 申請地 文字選択------------

  /**
   * 申請地選択画面へ遷移(初期状態は文字選択画面)
   * @param {Object} 選択済みの申請区分
   * @param {Object} 選択済みの申請区分ローカル保持用
   */
  @action
  nextCharacterSelectionView(checkedApplicationCategory:Object,checkedApplicationCategoryLocalSave:Object){
    this.lotNumberSearchPosDiffTop = -10;
    this.lotNumberSearchPosDiffLeft = 13;
    this.islotNumberSearchViewIndependent = false;
    this.showApplicationCategorySelection = false;
    this.checkedApplicationCategory = checkedApplicationCategory;
    this.checkedApplicationCategoryLocalSave = checkedApplicationCategoryLocalSave;
    this.terria.analytics?.logEvent(Category.CharacterSelection, CharacterSelection.panelOpened);
    this.showCharacterSelectionView();
    this.showMapSelectionView();
  }

  /**
   * 文字選択画面を表示
   */
  @action
  showCharacterSelectionView(){
    this.terria.analytics?.logEvent(Category.CharacterSelection, CharacterSelection.panelOpened);
    this.selectLotSearchResult = [];
    this.terria.setClickMode("");
    this.showCharacterSelection = true;
    this.searchDistrictId = null;
    this.searchDistrictName = null;
    this.setTopElement("CharacterSelection");
  }

  /**
   * 文字選択画面を閉じる
   */
  @action
  hideCharacterSelectionView() {
    this.selectLotSearchResult = [];
    this.terria.setClickMode("1");
    this.showCharacterSelection = false;
    this.searchDistrictId = null;
    this.searchDistrictName = null;
  }

  /**
   * 選択済みの申請地を変更
   */
  @action
  changeApplicationPlace() {
    this.selectLotSearchResult.forEach(element => {
      const index = this.applicationPlaceTemp.findIndex(obj => 
        obj.chibanId == element.chibanId
      );
      if(index < 0){
        this.applicationPlaceTemp.push(element);
      }
    })
    this.applicationPlace = Object.assign({},this.applicationPlaceTemp);
  }

  //------------UI004 申請地 地図選択------------

  /**
   * 地図選択画面を表示
   */
  @action
  showMapSelectionView(){
    this.terria.analytics?.logEvent(Category.MapSelection, MapSelection.panelOpened);
    this.showMapSelection = true;
    this.setTopElement("MapSelection");
  }

  /**
   * 地図選択画面を閉じる
   */
  @action
  hideMapSelectionView() {
    this.showMapSelection = false;
    if(!this.terria.authorityJudgment()){
      this.terria.setClickMode("");
    }
  }

  /**
  * 選択済み申請地の削除
  * @param {number} 削除対象のindex
  */
  @action
  deleteApplicationPlace(index:number) {
    this.applicationPlaceTemp.splice(index,1);
    this.applicationPlace = Object.assign({},this.applicationPlaceTemp);
  }

  /**
  * 地番からの選択済み申請地の削除
  * @param {any} 削除対象の地番
  */
  @action
  deleteApplicationPlaceByChiban(lotObj: any) {
    const index = this.applicationPlaceTemp.findIndex(obj => 
      obj.chibanId == lotObj.chibanId
    );
    if(index >= 0){
      this.applicationPlaceTemp.splice(index,1);
      this.applicationPlace = Object.assign({},this.applicationPlaceTemp);
      return true;
    }
    return false;
  }

  //------------UI005概況診断------------

  /**
   * 概況診断画面に遷移
   */
  @action
  nextGeneralConditionDiagnosisView() {
    this.showCharacterSelection = false;
    this.showMapSelection = false;
    this.terria.setClickMode("");
    this.terria.analytics?.logEvent(Category.GeneralConditionDiagnosis, GeneralConditionDiagnosis.panelOpened);
    this.showGeneralConditionDiagnosis = true;
    this.setTopElement("GeneralConditionDiagnosis");
  }

  /**
   * 概況診断画面を閉じる
   */
  @action
  hideGeneralConditionDiagnosisView() {
    this.initInputApplication();
    this.showGeneralConditionDiagnosis = false;
  }

  /**
   * 概況診断画面に戻る
   */
  @action
  backGeneralConditionDiagnosisView() {
    this.showEnterApplicantInformation = false;
    this.terria.analytics?.logEvent(Category.GeneralConditionDiagnosis, GeneralConditionDiagnosis.panelOpened);
    this.showGeneralConditionDiagnosis = true;
    this.setTopElement("GeneralConditionDiagnosis");
  }

  /**
   * 概況診断レポートの一時フォルダ名をセットする
   * @param {string} 一時フォルダ名
   */
  @action
  setFolderName(folderName:string){
    this.folderName = folderName;
  }

  //------------UI007申請者情報------------

  /**
   * 申請者情報画面に遷移
   * @param {Object} 概況診断結果
   */
  @action
  nextEnterApplicantInformationView(generalConditionDiagnosisResult:Object){
    this.generalConditionDiagnosisResult = generalConditionDiagnosisResult;
    this.showGeneralConditionDiagnosis = false;
    this.terria.analytics?.logEvent(Category.EnterApplicantInformation, EnterApplicantInformation.panelOpened);
    this.showEnterApplicantInformation = true;
    this.setTopElement("EnterApplicantInformation");
  }

  /**
   * 申請者情報画面を閉じる
   */
  @action
  hideEnterApplicantInformationView() {
    this.initInputApplication();
    this.showEnterApplicantInformation = false;
  }
  
  /**
   * 申請者情報画面に戻る
   * @param {Object} 申請ファイル一覧
   */
  @action
  backEnterApplicantInformationView(applicationFile:Object){
    this.applicationFile = applicationFile;
    this.showUploadApplicationInformation = false;
    this.terria.analytics?.logEvent(Category.EnterApplicantInformation, EnterApplicantInformation.panelOpened);
    this.showEnterApplicantInformation = true;
    this.setTopElement("EnterApplicantInformation");
  }

  //------------UI008 申請ファイル登録------------

  /**
   * 申請ファイル登録画面に遷移
   * @param {Object} 申請者情報
   */
  @action
  nextUploadApplicationInformationView(applicantInformation:Object){
    this.applicantInformation = applicantInformation;
    this.showEnterApplicantInformation = false;
    this.terria.analytics?.logEvent(Category.UploadApplicationInformation, UploadApplicationInformation.panelOpened);
    this.showUploadApplicationInformation = true;
    this.setTopElement("UploadApplicationInformation");
  }

  /**
   * 申請ファイル登録画面へ戻る
   */
  @action
  backUploadApplicationInformationView(){
    this.showConfirmApplicationDetails = false;
    this.terria.analytics?.logEvent(Category.UploadApplicationInformation, UploadApplicationInformation.panelOpened);
    this.showUploadApplicationInformation = true;
    this.setTopElement("UploadApplicationInformation");
  }

  /**
   * 申請ファイル登録画面を閉じる
   */
  @action
  hideUploadApplicationInformationView() {
    this.initInputApplication();
    this.showUploadApplicationInformation = false;
  }

  //------------UI009申請内容確認------------

  /**
   * 申請内容確認画面へ遷移
   * @param {Object} 申請ファイル一覧
   */
  @action
  nextConfirmApplicationDetailsView(applicationFile:Object){
    this.applicationFile = applicationFile;
    this.showUploadApplicationInformation = false;
    this.terria.analytics?.logEvent(Category.ConfirmApplicationDetails, ConfirmApplicationDetails.panelOpened);
    this.showConfirmApplicationDetails = true;
    this.setTopElement("ConfirmApplicationDetails");
  }

  /**
   * 申請内容確認画面を閉じる
   */
  @action
  hideConfirmApplicationDetailsView() {
    this.initInputApplication();
    this.showConfirmApplicationDetails = false;
  }

  /**
   * 申請登録完了画面へ遷移
   * @param {string} ログインID
   * @param {string} パスワード
   */
  @action
  nextApplicationCompletedView(loginId:string,password:string){
    this.showConfirmApplicationDetails = false;
    this.customMessageContent = "ログインID："+loginId+"<br/>パスワード："+password+"<br/>" + this.customMessageContent;
    this.initInputApplication();
    this.terria.analytics?.logEvent(Category.CustomMessage, CustomMessage.panelOpened);
    this.showCustomMessage = true;
    this.setTopElement("CustomMessage");
  }

  //------------UI011申請ID/パスワード照合------------

  /**
   * 申請ID/パスワード照合画面を表示
   */
  @action
  showAnswerLoginView(){
    this.terria.analytics?.logEvent(Category.AnswerLogin, AnswerLogin.panelOpened);
    this.showAnswerLogin = true;
    this.setTopElement("AnswerLogin");
  }

  /**
   * 申請ID/パスワード照合画面を閉じる
   */
  @action
  hideAnswerLoginView() {
    this.showAnswerLogin = false;
  }

  //------------UI012申請内容確認------------

  /**
   * 申請内容確認画面を表示
   * @param {Object} 申請の回答内容
   */
  @action
  showAnswerContentView(answerContent:Object){
    this.answerContent = answerContent;
    this.showAnswerLogin = false;
    this.terria.analytics?.logEvent(Category.AnswerContent, AnswerContent.panelOpened);
    this.showAnswerContent = true;
    this.setTopElement("AnswerContent");
  }

  /**
   * 申請内容確認画面を閉じる
   */
  @action
  hideAnswerContentView() {
    this.answerContent = {};
    this.showAnswerContent = false;
  }

  //------------UI007以降の入力値をリセット------------
  @action
  initInputApplication(){
    this.applicantInformation = [];
    this.applicationFile = {};
    this.checkedApplicationCategory = [];
    this.checkedApplicationCategoryLocalSave = [];
    this.applicationPlace = [];
    this.applicationPlaceTemp = [];
    this.searchDistrictName = null;
    this.searchDistrictId = null;
    this.generalConditionDiagnosisResult = {};
    if(!this.terria.authorityJudgment()){
      this.terria.setClickMode("");
    }
  }

  //------------UI104申請情報検索------------

  /**
   * 申請情報検索画面を表示
   */
  @action
  showApplicationInformationSearchView(){
    this.terria.analytics?.logEvent(Category.ApplicationInformationSearch, ApplicationInformationSearch.panelOpened);
    this.showApplicationInformationSearch = true;
    this.setTopElement("ApplicationInformationSearch");
  }

  /**
   * 申請情報検索画面を閉じる
   */
  @action
  hideApplicationInformationSearchView() {
    this.showApplicationInformationSearch = false;
  }

  //------------UI106申請情報詳細------------

  /**
   * 申請情報詳細画面へ遷移
   * @param {string} 申請ID
   */
  @action
  nextApplicationDetailsView(applicationInformationSearchForApplicationId:string){
    if(!this.showApplicationDetails){
      this.applicationInformationSearchForApplicationId = applicationInformationSearchForApplicationId;
      this.terria.analytics?.logEvent(Category.ApplicationDetails, ApplicationDetails.panelOpened);
      this.showApplicationDetails = true;
      this.setTopElement("ApplicationDetails");
    }
  }

  /**
   * 申請情報詳細画面を閉じる
   */
  @action
  hideApplicationDetailsView() {
    this.showApplicationDetails = false;
  }

  //------------UI108回答入力------------

  /**
   * 回答入力画面へ遷移
   */
  @action
  nextAnswerInputView(){
    this.terria.analytics?.logEvent(Category.AnswerInput, AnswerInput.panelOpened);
    this.showAnswerInput = true;
    this.setTopElement("AnswerInput");
  }

  /**
   * 回答入力画面を閉じる
   */
  @action
  hideAnswerInputView() {
    this.showAnswerInput = false;
  }

  //------------UI109回答完了------------

  /**
   * 回答完了画面へ遷移
   */
  @action
  nextAnswerCompleteView(){
    this.showAnswerInput = false;
    this.initInputApplication();
    this.terria.analytics?.logEvent(Category.CustomMessage, CustomMessage.panelOpened);
    this.showCustomMessage = true;
    this.setTopElement("CustomMessage");
  }

  //------------UI110回答通知完了------------

  /**
   * 回答通知完了画面へ遷移
   */
  @action
  nextAnswerNotificationView(){
    this.showApplicationDetails = false;
    this.initInputApplication();
    this.terria.analytics?.logEvent(Category.CustomMessage, CustomMessage.panelOpened);
    this.showCustomMessage = true;
    this.setTopElement("CustomMessage");
  }

  //------------サブ画面申請一覧------------

  /**
   * 申請一覧のサブ画面を表示
   * @param {Object} 申請一覧
   */
  @action
  showApplicationListView(applicationList:Object){
    this.applicationList = applicationList;
    this.terria.analytics?.logEvent(Category.ApplicationList, ApplicationList.panelOpened);
    this.showApplicationList = true;
    this.setTopElement("ApplicationList");
  }

  /**
   * 申請一覧のサブ画面を閉じる
   */
  @action
  hideApplicationListView() {
    this.showApplicationList = false;
  }

  /**
   * ↑カスタム機能
   */
  @computed
  get breadcrumbsShown() {
    return (
      this.previewedItem !== undefined ||
      this.userDataPreviewedItem !== undefined
    );
  }

  @computed
  get isToolOpen() {
    return this.currentTool !== undefined;
  }

  @computed
  get hideMapUi() {
    return (
      this.terria.notificationState.currentNotification !== undefined &&
      this.terria.notificationState.currentNotification!.hideUi
    );
  }

  get isMapZooming() {
    return this.terria.currentViewer.isMapZooming;
  }

  /**
   * Returns true if the user is currently interacting with the map - like
   * picking a point or drawing a shape.
   */
  @computed
  get isMapInteractionActive() {
    return this.terria.mapInteractionModeStack.length > 0;
  }
}

interface Tool {
  toolName: string;
  getToolComponent: () => React.ComponentType | Promise<React.ComponentType>;

  showCloseButton: boolean;
  params?: any;
}
