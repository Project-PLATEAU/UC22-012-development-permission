import { observer } from "mobx-react";
import React, { useMemo } from "react";
import PropTypes from "prop-types";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Box from "../../../../Styled/Box";
import Spacing from "../../../../Styled/Spacing";
import Text from "../../../../Styled/Text";
import Input from "../../../../Styled/Input";
import Button, { RawButton } from "../../../../Styled/Button";
import CustomStyle from "./scss/lot-number-search.scss";
import ScreenButton from "../../../Map/HelpButton/help-button.scss";
import {
    getShareData
} from "../../../Map/Panels/SharePanel/BuildShareLink";
import Common from "../../../AdminDevelopCommon/CommonFixedValue.json";
import Cartographic from "terriajs-cesium/Source/Core/Cartographic";
import Ellipsoid from "terriajs-cesium/Source/Core/Ellipsoid";
import GeoJsonCatalogItem from "../../../../Models/Catalog/CatalogItems/GeoJsonCatalogItem";
import CommonStrata from "../../../../Models/Definition/CommonStrata";
import Config from "../../../../../customconfig.json";
import webMapServiceCatalogItem from '../../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import CustomStyleDistrict from "./scss/district-name-search.scss";
import CustomStyleKana from "./scss/kana-district-name-search.scss";
import sampleTerrainMostDetailed from "terriajs-cesium/Source/Core/sampleTerrainMostDetailed";

/**
 * 地番検索画面
 */
@observer
class LotNumberSearch extends React.Component {
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired
    }
    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            //地番
            lotNumber: "",
            //かな文字
            kanaText: "",
            //検索結果一覧テーブル定義情報
            table: [],
            //検索結果
            searchResult: [],
            //ボタン非活性フラグ
            disabled: false,
            //町丁名一覧
            districtList: [],
            //かなfilter済みの町丁名一覧
            filteredList: [],
        };
        // カナ検索コンポーネント（保持）
        this.props.viewState.setKanaDistrictNameSearch((props) => {
            const kanaText = props.kanaText;
            const filteredList = props.filteredList;
            const keyBoardList = [
                ["", "あ", "か", "さ", "た", "な", "は", "ま", "や", "ら", "わ", ""],
                ["", "い", "き", "し", "ち", "に", "ひ", "み", "", "り", "", ""],
                ["", "う", "く", "す", "つ", "ぬ", "ふ", "む", "ゆ", "る", "を", ""],
                ["", "え", "け", "せ", "て", "ね", "へ", "め", "", "れ", "", ""],
                ["", "お", "こ", "そ", "と", "の", "ほ", "も", "よ", "ろ", "ん", ""]
            ];
            const boxCssText = `
                    position: absolute;
                    z-index: 9989;
                    top: 50%;
                    left:50%;
                    `;
            return (
                <Box
                    displayInlineBlock
                    backgroundColor={this.props.theme.textLight}
                    styledWidth={"520px"}
                    styledHeight={"500px"}
                    fullHeight
                    overflow={"auto"}
                    id="KanaDistrictNameSearchFrame"
                    css={boxCssText}
                    className={CustomStyleKana.custom_frame}>
                    <Box position="absolute" paddedRatio={3} topRight>
                        <RawButton onClick={() => {
                            this.props.viewState.hideKanaDistrictNameSearchView();
                        }}>
                            <StyledIcon
                                styledWidth={"16px"}
                                fillColor={this.props.theme.textLight}
                                opacity={"0.5"}
                                glyph={Icon.GLYPHS.closeLight}
                                css={`
                            cursor:pointer;
                          `}
                            />
                        </RawButton>
                    </Box>
                    <nav className={CustomStyleKana.custom_nuv}>
                        かな検索
                    </nav>
                    <Box
                        paddedHorizontally={5}
                        paddedVertically={10}
                        centered
                        displayInlineBlock
                        className={CustomStyleKana.custom_content}
                    >
                        <div className={CustomStyleKana.box_input_area}>
                            <Input
                                light={true}
                                dark={false}
                                white={true}
                                type="text"
                                value={kanaText}
                                placeholder=""
                                id="kanaText"
                                style={{ width: 100 + "%" }}
                                autocomplete="off"
                                readOnly
                            />
                            <button
                                className={CustomStyleKana.clear_button}
                                onClick={evt => {
                                    evt.preventDefault();
                                    evt.stopPropagation();
                                    this.setState({ kanaText: "" });
                                    this.setState({ filteredList: [] });
                                }}
                                id="clearBtn"
                                style={{ width: 30 + "%", lineHeight: 2 + "em", }}
                            >
                                <span>クリア</span>
                            </button>
                        </div>
                        <div className={CustomStyleKana.kana_keyboard_area}>
                            {
                                Object.keys(keyBoardList).map(idx => (
                                    <div
                                        className={CustomStyleKana.kana_row} key={"keyBoardList"+idx}
                                    >
                                        {Object.keys(keyBoardList[idx]).map(idx2 => (
                                            <div
                                                className={keyBoardList[idx][idx2] === "" ? CustomStyleKana.kana_cell_empty : CustomStyleKana.kana_cell}
                                                onClick={
                                                    evt => {
                                                        evt.preventDefault();
                                                        evt.stopPropagation();
                                                        if (keyBoardList[idx][idx2] != "") {
                                                            this.inputKanaText(keyBoardList[idx][idx2]);
                                                        }
                                                    }
                                                }
                                                key={"kana"+idx2}
                                            >{keyBoardList[idx][idx2]}
                                            </div>
                                        ))}
                                    </div>
                                ))
                            }
                        </div>
                        <div className={CustomStyleKana.district_name_area}>
                            <table
                                className={CustomStyleKana.district_list_table}
                            >
                                <tbody>
                                    {Object.keys(filteredList).map(idx => (
                                        <tr className={CustomStyleKana.district_list_table_row} key={"filteredList"+idx}>
                                            <td
                                                onClick={evt => {
                                                    evt.preventDefault();
                                                    evt.stopPropagation();
                                                    this.selectDistrictName(filteredList[idx].id, filteredList[idx].name);
                                                }
                                                }
                                            >{filteredList[idx].name}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                        <div className={CustomStyleKana.select_btn_area}>
                        </div>
                    </Box>
                </Box>
            );
        })

        // 町丁名検索コンポーネント　memo化（不要な再レンダリングを防止）
        if (this.props.viewState.DistrictNameSearch === "") {
            this.props.viewState.setDistrictNameSearch(React.memo(props => {
                console.log("React.memo DistrictNameSearch");
                const districtList = props.districtList;
                return (<Box
                    displayInlineBlock
                    backgroundColor={this.props.theme.textLight}
                    styledWidth={"300px"}
                    styledHeight={"300px"}
                    fullHeight
                    overflow={"auto"}
                    id="DistrictNameSearchFrame"
                    css={`
                        position: absolute;
                        z-index: 9988;
                        top: 41%;
                        left: 45%;
                        `}
                    className={CustomStyleDistrict.custom_frame}>
                    <div
                        className={CustomStyleDistrict.content_col}
                    ><Text>町名・町丁名</Text></div>
                    <div
                        className={CustomStyleDistrict.content_col}
                    >
                        <div style={{ textAlign: "right" }}>
                            <RawButton onClick={() => {
                                this.props.viewState.hideKanaDistrictNameSearchView();
                                this.props.viewState.hideDistrictNameSearchView();
                            }}>
                                <StyledIcon
                                    styledWidth={"16px"}
                                    fillColor={this.props.theme.textLight}
                                    opacity={"0.5"}
                                    glyph={Icon.GLYPHS.closeLight}
                                    css={`
                    cursor:pointer;
                    float: right;
                  `}
                                />
                            </RawButton>
                        </div>
                        <div className={CustomStyleDistrict.district_list_table_wrapper}>
                            <table
                                className={CustomStyleDistrict.district_list_table}
                            >
                                <tbody>
                                    {Object.keys(districtList).map(idx => (
                                        <tr className={CustomStyleDistrict.district_list_table_row} key={"districtList"+idx}>
                                            <td
                                                onClick={evt => {
                                                    evt.preventDefault();
                                                    evt.stopPropagation();
                                                    this.selectDistrictName(districtList[idx].id, districtList[idx].name);
                                                }
                                                }
                                            >{districtList[idx].name}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                        <div>
                            <buttton
                                className={CustomStyleDistrict.custom_button}
                                onClick={evt => {
                                    this.props.viewState.showKanaDistrictNameSearchView();
                                }}
                            >かな検索</buttton>
                        </div></div>
                </Box>);
            }, (prevProps, nextProps) => {
                return prevProps?.districtList[0]?.id === nextProps.districtList[0]?.id
            }))
        }

        // 検索結果一覧 memo化（不要な再レンダリングを防止）
        if (this.props.viewState.SearchRsultContent === "") {
            this.props.viewState.setSearchRsultContent(React.memo(props => {
                console.log("React.memo SearchRsultContent");
                const searchResult = props.searchResult;
                const table = props.table;
                const isViewIndependent = props.isViewIndependent;
                return (<>
                    {Object.keys(searchResult).map(idx => {
                        return (<tr key={searchResult[idx].chibanId}>
                            {!isViewIndependent && <td style={{ width: "8%" }}>
                                <input type="checkbox"
                                    onClick={
                                        evt => {
                                            this.switchSelectLotNumber(searchResult[idx]);
                                        }
                                    }
                                    style={{ display: "block", margin: "0 auto" }}
                                    defaultChecked={false}
                                ></input>
                            </td>}
                            {this.props.terria.authorityJudgment() && <td style={{ width: "20%" }}>{Common.status[searchResult[idx]?.status]}</td>}
                            {Object.keys(table).map(tableKey => (
                                <>
                                    {searchResult[idx]?.attributes && table[tableKey]?.responseKey &&
                                        <td key={"table" + tableKey} style={{ width: table[tableKey]?.tableWidth + "%" }}>{searchResult[idx]?.attributes[table[tableKey]?.responseKey]}</td>
                                    }
                                </>
                            ))}
                            <td style={{ width: "10%" }}>
                                <button
                                    className={ScreenButton.helpBtn}
                                    onClick={evt => {
                                        this.focusMapPlace(searchResult[idx]?.maxlon,searchResult[idx]?.maxlat,searchResult[idx]?.minlon,searchResult[idx]?.minlat,searchResult[idx]?.lon, searchResult[idx]?.lat);
                                    }}
                                    style={{ width: 80 + "px", lineHeight: 2 + "em", padding: 2 + "px", margin: "0 auto" }}
                                >
                                    <StyledIcon
                                        styledWidth={"40px"}
                                        fillColor={this.props.theme.textLight}
                                        opacity={"0.5"}
                                        glyph={Icon.GLYPHS.mapbutton}
                                        css={`
                                    cursor:pointer;
                                    height: 20px;
                                    width: 20px;
                                `}
                                    />
                                </button>
                            </td>
                        </tr>)
                    })}
                </>);
            }, (prevProps, nextProps) => {
                return (prevProps?.searchResult[0]?.chibanId === nextProps?.searchResult[0]?.chibanId)
            }))
        }
    }

    // 初期描画
    componentDidMount() {
        document.getElementById("customloader").style.display = "none"
        // テーブル定義取得
        fetch(Config.config.apiUrl + "/lotnumber/columns")
            .then(res => res.json())
            .then(res => {
                if (Object.keys(res).length > 0 && !res.status) {
                    this.setState({ table: res });
                } else {
                    alert("地番情報のテーブル定義の取得に失敗しました。再度操作をやり直してください。");
                }
            }).catch(error => {
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            });
        //町丁目一覧取得
        fetch(Config.config.apiUrl + "/lotnumber/districts")
            .then(res => res.json())
            .then(res => {
                if (Object.keys(res).length > 0) {
                    this.setState({ districtList: res });
                } else {
                    alert("町丁名一覧の取得に失敗しました。再度操作をやり直してください。");
                }
            }).catch(error => {
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            });
        if (this.props.viewState.islotNumberSearchViewIndependent) {
            this.draggable(document.getElementById('LotNumberSearchFrameDrag'), document.getElementById('LotNumberSearchFrame'));
        } else {
            this.draggable(document.getElementById('LotNumberSearchFrameDrag'), document.getElementById('CharacterSelection'));
        }
    }

    /*
    * 地番検索
    */
    searchLotNumber() {
        this.setState({ searchResult: [], disabled: true });
        document.getElementById("customloader").style.display = "block";
        let path = "/lotnumber/search/estabrishment";
        if (this.props.terria.authorityJudgment()) {
            path = "/lotnumber/search/goverment";
        }
        if ((!this.props.viewState.searchDistrictId || this.props.viewState.searchDistrictId === "") &&
            (!this.state.lotNumber || this.state.lotNumber === "")) {
            document.getElementById("customloader").style.display = "none";
            alert("町丁名または地番は必須入力です。");
            this.setState({ disabled: false });
            return false;
        }
        //地番検索を実施
        fetch(Config.config.apiUrl + path, {
            method: 'POST',
            body: JSON.stringify({
                districtId: this.props.viewState.searchDistrictId,
                chiban: this.state.lotNumber
            }),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
            .then(res => res.json())
            .then(res => {
                if (Object.keys(res).length > 0 && !res.status) {
                    if (!this.props.viewState.showMapSelection) {
                        let layrerName = "";
                        let displayName = "";
                        let paramName = "";
                        let layerFlg = false;
                        if (this.props.terria.authorityJudgment()) {
                            layrerName = Config.layer.lotnumberSearchLayerNameForGoverment;
                            displayName = Config.layer.lotnumberSearchLayerDisplayNameForGoverment;
                            paramName = Config.layer.lotnumberSearchViewParamNameForGoverment;
                        } else {
                            layrerName = Config.layer.lotnumberSearchLayerNameForBusiness;
                            displayName = Config.layer.lotnumberSearchLayerDisplayNameForBusiness;
                            paramName = Config.layer.lotnumberSearchViewParamNameForBusiness;
                        }
                        const wmsUrl = Config.config.geoserverUrl;
                        const items = this.state.terria.workbench.items;
                        try {
                            for (const aItem of items) {
                                if (aItem.uniqueId === layrerName) {
                                    aItem.setTrait(CommonStrata.user,
                                        "parameters",
                                        {
                                            "viewparams": paramName + res?.map(obj => { return obj.chibanId }).filter(chibanId => { return chibanId !== null }).join("_"),
                                        });
                                    aItem.loadMapItems();
                                    layerFlg = true;
                                }
                            }
                            if(!layerFlg){
                                const item = new webMapServiceCatalogItem(layrerName, this.state.terria);
                                item.setTrait(CommonStrata.definition, "url", wmsUrl);
                                item.setTrait(CommonStrata.user, "name", displayName);
                                item.setTrait(CommonStrata.user, "allowFeaturePicking", true);
                                item.setTrait(
                                    CommonStrata.user,
                                    "layers",
                                    layrerName);
                                item.setTrait(CommonStrata.user,
                                    "parameters",
                                    {
                                        "viewparams": paramName + res?.map(obj => { return obj.chibanId }).filter(chibanId => { return chibanId !== null }).join("_"),
                                    });
                                item.loadMapItems();
                                this.state.terria.workbench.add(item);
                            }
                        } catch (error) {
                            console.error('処理に失敗しました', error);
                        }
                    }
                    this.setState({ searchResult: res });
                } else if (res.status) {
                    this.setState({ searchResult: [] });
                    alert(res.status + "エラーが発生しました");
                } else {
                    this.setState({ searchResult: [] });
                    alert("検索結果はありません");
                }
            }).catch(error => {
                this.setState({ searchResult: [] });
                console.error('処理に失敗しました', error);
                alert('処理に失敗しました');
            }).finally(() => {
                document.getElementById("customloader").style.display = "none";
                this.setState({ disabled: false });
                if (Object.keys(this.state.searchResult).length > 5000) {
                    alert("該当件数が5,000件を超えました。\n最初の5,000件までを表示します。");
                }
            });;
    }

    /**
     * フォーカス処理
     * @param {number} maxLon 最大経度
     * @param {number} maxLat 最大緯度
     * @param {number} minLon 最小経度
     * @param {number} minLat 最小緯度
     * @param {number} lon 経度
     * @param {number} lat 緯度
     */
     focusMapPlace(maxLon, maxLat, minLon, minLat, lon, lat) {
        // 3dmodeにセット
        this.props.viewState.set3dMode();
        // 地点の経度緯度にピンにマークを付ける
        this.clearSamplePoint();
        const pointItem = new GeoJsonCatalogItem("対象地点", this.state.terria);
        pointItem.setTrait(CommonStrata.user, "geoJsonData", {
            type: "Feature",
            properties: {
                "marker-color": "#dc143c",
                "id": 1,
                "経度": lon,
                "緯度": lat,
            },
            geometry: {
                type: "Point",
                coordinates: [lon, lat]
            }
        });
        pointItem.loadMapItems();
        this.state.terria.workbench.add(pointItem);
        //現在のカメラ位置等を取得
        const currentSettings = getShareData(this.state.terria, this.props.viewState);
        const currentCamera = currentSettings.initSources[0].initialCamera;
        let newCamera = Object.assign(currentCamera);
        //新規の表示範囲を設定
        let currentLonDiff = Math.abs(maxLon - minLon);
        let currentLatDiff = Math.abs(maxLat - minLat);
        newCamera.north = maxLon + currentLatDiff / 2;
        newCamera.south = minLon - currentLatDiff / 2;
        newCamera.east = maxLat + currentLonDiff / 2;
        newCamera.west = minLat - currentLonDiff / 2;
        //camera.positionを緯度経度に合わせて設定
        const scene = this.props.terria.cesium.scene;
        const terrainProvider = scene.terrainProvider;
        const positions = [Cartographic.fromDegrees(lon, minLat)];
        let height = 0;
        sampleTerrainMostDetailed(terrainProvider, positions).then((updatedPositions) => {
            height = updatedPositions[0].height
            let coord_wgs84 = Cartographic.fromDegrees(lon, minLat, parseFloat(height) + parseInt((400000 * currentLatDiff )) + 200 );
            let coord_xyz = Ellipsoid.WGS84.cartographicToCartesian(coord_wgs84);
            newCamera.position = { x: coord_xyz.x, y: coord_xyz.y, z: coord_xyz.z - parseInt((300000 * currentLatDiff )) - 170 };
            //カメラの向きは統一にさせる
            newCamera.direction = { x: this.props.terria.focusCameraDirectionX, y: this.props.terria.focusCameraDirectionY, z: this.props.terria.focusCameraDirectionZ };
            newCamera.up = { x: this.props.terria.focusCameraUpX, y: this.props.terria.focusCameraUpY, z:this.props.terria.focusCameraUpZ };
            this.state.terria.currentViewer.zoomTo(newCamera, 3);
        })
    }

    //ピンマークの削除
    clearSamplePoint() {
        const items = this.state.terria.workbench.items;
        for (const aItem of items) {
            if (aItem.uniqueId === '対象地点') {
                this.state.terria.workbench.remove(aItem);
                aItem.loadMapItems();
            }
        }
    }

    /**
     * 地番の選択状態を切り替える
     * @param {Object} searchResultObj 対象地番
     */
    switchSelectLotNumber(searchResultObj) {
        this.props.viewState.switchLotNumberSelect(searchResultObj);
    }

    /**
     * 町丁名選択結果を親コンポーネントに返す
     * @param {string} districtId
     * @param {string} districtName
     */
    selectDistrictName(districtId, districtName) {
        this.props.viewState.setDistrictName(districtId, districtName);
        this.props.viewState.hideKanaDistrictNameSearchView();
        this.props.viewState.hideDistrictNameSearchView();

    }

     /**
     * かなキーボード入力
     * @param {string} txt
     */
    inputKanaText(txt) {
        const currentText = this.state.kanaText;
        const newText = currentText.concat(txt)
        this.setState({ kanaText: newText });
        this.filterByInput(newText);
    }

    /**
     * リストをかな入力でフィルタする
     * @param {string} inputTxt
     */
    filterByInput(inputTxt) {
        const districtList = this.state.districtList;
        if (inputTxt !== "") {
            const filteredList = districtList.filter((aDist) => aDist.kana.startsWith(inputTxt));
            this.setState({ filteredList: filteredList });
        } else {
            this.setState({ filteredList: [] });
        }
    }

    //close処理
    close() {
        this.clearSamplePoint();
        const items = this.state.terria.workbench.items;
        for (const aItem of items) {
            if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForBusiness || aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForGoverment) {
                this.state.terria.workbench.remove(aItem);
                aItem.loadMapItems();
            }
        }
        this.props.viewState.hideLotNumberSearchView();
    }

    //clear処理
    clear() {
        this.props.viewState.setDistrictName(null, null);
        document.getElementById("districtName").value = "";
        this.setState({ lotNumber: "" });
    }

    /**
     * コンポーネントドラッグ操作
     * @param {Object} ドラッグ操作対象
     * @param {Object} ドラッグ対象
     */
    draggable(target, content) {
        target.onmousedown = function (e) {
            //町丁名入力欄、地番入力欄、検索ボタン、クリアボタンはドラッグ操作対象外とする
            if(e.target.id !== "districtName" && e.target.id !=="lotNumber" && e.target.id !=="searchBtn" && e.target.id !=="lotNumberClearBtn"){
                document.onmousemove = mouseMove;
            }
        };
        document.onmouseup = function () {
            document.onmousemove = null;
        };
        function mouseMove(e) {
            var event = e ? e : window.event;
            content.style.top = (event.clientY + (parseInt(content.clientHeight) / 2) - 5) + 'px';
            content.style.left = (event.clientX) + 'px';
        }
    }

    render() {
        const districtName = this.props.viewState.searchDistrictName;
        const topDiff = this.props.viewState.lotNumberSearchPosDiffTop;
        const leftDiff = this.props.viewState.lotNumberSearchPosDiffLeft;
        const isViewIndependent = this.props.viewState.islotNumberSearchViewIndependent;
        const lotNumber = this.state.lotNumber;
        const searchResult = this.state.searchResult;
        const table = this.state.table;
        const disabled = this.state.disabled;
        const SearchRsultContent = this.props.viewState.SearchRsultContent;
        const DistrictNameSearch = this.props.viewState.DistrictNameSearch;
        const districtList = this.state.districtList;
        const kanaText = this.state.kanaText;
        const filteredList = this.state.filteredList;
        const KanaDistrictNameSearch = this.props.viewState.KanaDistrictNameSearch;
        const boxCssText = `
            position: fixed;
            z-index: 9987;
            top: ` + (60 + topDiff) + `%;
            left: ` + (30 + leftDiff) + `%;
            `;
        //検索結果数 一度に表示できる最大件数は5000件を限度とする
        let searchResultCount = 0;
        if (Object.keys(searchResult).length > 5000) {
            searchResultCount = 5000;
        } else {
            searchResultCount = Object.keys(searchResult).length;
        }
        return (
            <>
                <Box
                    displayInlineBlock
                    backgroundColor={this.props.theme.textLight}
                    styledWidth={"520px"}
                    styledHeight={"500px"}
                    fullHeight
                    overflow={"auto"}
                    id="LotNumberSearchFrame"
                    css={boxCssText}
                    className={CustomStyle.custom_frame}
                >
                    {this.props.viewState.showDistrictNameSearch && (
                        <DistrictNameSearch districtList={districtList} />
                    )}
                    {this.props.viewState.showKanaDistrictNameSearch && (
                        <KanaDistrictNameSearch kanaText={kanaText} filteredList={filteredList} />
                    )}
                    <Box
                        column
                        position="absolute"
                        paddedRatio={3}
                        topRight
                        styledHeight="110px"
                        styledPadding="5px"
                        className={CustomStyle.custom_header}
                        id="LotNumberSearchFrameDrag"
                    >
                        <Box styledHeight={"45%"} styledWidth={"100%"}>
                            <div style={{ width: 20 + "%", height: 100 + "%" }}><nav>地番検索</nav></div>
                            <div style={{ width: 10 + "%", height: 100 + "%", textAlign: "right", verticalAlign: "middle" }}>
                                <Text style={{ lineHeight: 2 + "em" }}>町丁名</Text></div>
                            <div style={{ width: 45 + "%", padding: 5 + "px", height: 100 + "%" }}>
                                <Input
                                    light={true}
                                    dark={false}
                                    white={true}
                                    type="text"
                                    value={districtName ?? ''}
                                    placeholder="一覧から町丁名を選択"
                                    id="districtName"
                                    style={{ width: 100 + "%" }}
                                    onFocus={evt => {
                                        evt.preventDefault();
                                        evt.stopPropagation();
                                        this.props.viewState.showDistrictNameSearchView();
                                    }}
                                    autoComplete="off"
                                    readOnly
                                /></div>
                            <div style={{ width: 20 + "%", padding: 5 + "px", height: 100 + "%" }} id="lotNumberClearBtn">
                                <button
                                    className={CustomStyle.clear_button_in_header}
                                    onClick={evt => {
                                        evt.preventDefault();
                                        evt.stopPropagation();
                                        this.clear();
                                    }}
                                    disabled={disabled}
                                >
                                    <span>クリア</span>
                                </button>
                            </div>
                            <div style={{ width: 5 + "%", padding: 5 + "px", height: 100 + "%", textAlign: "right" }}>
                                {isViewIndependent &&
                                    <RawButton onClick={evt => {
                                        evt.preventDefault();
                                        evt.stopPropagation();
                                        this.close();
                                    }}>
                                        <StyledIcon
                                            styledWidth={"16px"}
                                            fillColor={this.props.theme.textLight}
                                            opacity={"0.5"}
                                            glyph={Icon.GLYPHS.closeLight}
                                            css={`
                                                cursor:pointer;
                                            `}
                                        />
                                    </RawButton>
                                }
                            </div>
                        </Box>
                        <Box styledHeight={"45%"} styledWidth={"100%"}>
                            <div style={{ width: 20 + "%", height: 100 + "%" }}>&nbsp;</div>
                            <div style={{ width: 10 + "%", height: 100 + "%", textAlign: "right", verticalAlign: "middle" }}>
                                <Text style={{ lineHeight: 2 + "em" }}>地番</Text>
                            </div>
                            <div style={{ width: 45 + "%", padding: 5 + "px", height: 100 + "%" }}>
                                <Input
                                    light={true}
                                    dark={false}
                                    white={true}
                                    type="text"
                                    value={lotNumber ?? ''}
                                    placeholder="地番を入力(例:1-1-1)"
                                    id="lotNumber"
                                    style={{ width: 100 + "%" }}
                                    onChange={e => this.setState({ lotNumber: e.target.value })}
                                />
                            </div>
                            <div style={{ width: 27 + "%", padding: 5 + "px", height: 100 + "%" }} id="searchBtn">
                                <button
                                    className={CustomStyle.button_in_header}
                                    onClick={evt => {
                                        evt.preventDefault();
                                        evt.stopPropagation();
                                        this.props.viewState.removeLosNumberSelect();
                                        this.searchLotNumber();
                                    }}
                                    disabled={disabled}
                                >
                                    <span>検索</span>
                                </button>
                            </div>
                        </Box>
                        <div style={{ position: "relative", top: -10 + "px", fontSize: ".7em", width: "150px" }}>検索結果件数：{Number(searchResultCount).toLocaleString()} 件</div>
                    </Box>
                    <Box
                        column
                        centered
                        displayInlineBlock
                        className={CustomStyle.custom_content}
                    >
                        <div id="customloader" className={CustomStyle.customloaderParent}>
                            <img className={CustomStyle.customloader} src="./images/loader.gif" />
                        </div>
                        <table className={CustomStyle.result_table}>
                            <thead>
                                {Object.keys(table).length > 0 && (
                                    <tr>
                                        {!isViewIndependent && <th style={{ width: "8%" }}></th>}
                                        {this.props.terria.authorityJudgment() && <th style={{ width: "20%" }}>申請</th>}
                                        {Object.keys(table).map(tableKey => (
                                            <th key={"tableKey"+tableKey} style={{ width: table[tableKey].tableWidth + "%" }}>{table[tableKey].displayColumnName}</th>
                                        ))}
                                        <th style={{ width: "10%" }}><div style={{ width: "80px" }}></div></th>
                                    </tr>
                                )}
                            </thead>
                            <tbody>
                                {SearchRsultContent &&
                                    <SearchRsultContent table={table} searchResult={searchResult} isViewIndependent={isViewIndependent} />
                                }
                            </tbody>
                        </table>
                    </Box>
                    <Spacing bottom={1} />
                </Box>
            </>
        );
    }
}
export default withTranslation()(withTheme(LotNumberSearch));