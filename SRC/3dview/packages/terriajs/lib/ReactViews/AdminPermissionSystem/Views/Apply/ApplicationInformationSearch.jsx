import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import Text from "../../../../Styled/Text";
import Input from "../../../../Styled/Input";
import Box from "../../../../Styled/Box";
import Button, { RawButton } from "../../../../Styled/Button";
import Select from "../../../../Styled/Select";
import CustomStyle from "./scss/application-information-search.scss";
import CommonStrata from "../../../../Models/Definition/CommonStrata";
import webMapServiceCatalogItem from '../../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import Config from "../../../../../customconfig.json";
import {
    getShareData
} from "../../../Map/Panels/SharePanel/BuildShareLink";
import Cartographic from "terriajs-cesium/Source/Core/Cartographic";
import Ellipsoid from "terriajs-cesium/Source/Core/Ellipsoid";
import sampleTerrainMostDetailed from "terriajs-cesium/Source/Core/sampleTerrainMostDetailed";
/**
 * 申請情報検索画面
 */
@observer
class ApplicationInformationSearch extends React.Component {

    static displayName = "ApplicationInformationSearch";

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
            //申請者情報(検索値)
            applicantInformation: [],
            //申請状態(検索値)
            status: [],
            //部署(検索値)
            department: [],
            //申請区分検索対象の画面情報(検索値)
            selectedScreen: [[], [], []],
            //検索結果テーブル定義情報
            table: [],
            //申請区分画面情報
            screen: [],
            //検索結果
            searchValue: []
        };
    }

    //サーバからデータを取得
    componentDidMount() {

        fetch(Config.config.apiUrl + "/application/search/columns")
            .then(res => res.json())
            .then(res => {
                if (Object.keys(res).length > 0 && !res.status) {
                    this.setState({
                        table: res
                    });
                } else if (res.status) {
                    alert(res.status + "エラーが発生しました");
                } else {
                    alert("申請情報検索結果表示項目一覧取得に失敗しました。再度操作をやり直してください。");
                }
            }).catch(error => {
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
                this.props.viewState.hideApplicationInformationSearchView();
            });

        fetch(Config.config.apiUrl + "/application/search/conditions")
            .then(res => res.json())
            .then(res => {
                if (Object.keys(res).length > 0) {
                    let selectedScreen = this.state.selectedScreen;
                    Object.keys(selectedScreen).map(key => {
                        selectedScreen[key] = JSON.parse(JSON.stringify(res.applicationCategories));
                    })
                    document.getElementById("customloaderSearch").style.display = "none";
                    this.setState({
                        applicantInformation: res.applicantInformationItemForm,
                        screen: res.applicationCategories,
                        status: res.status,
                        department: res.department,
                        selectedScreen: selectedScreen
                    });
                } else {
                    alert("申請情報検索条件一覧取得に失敗しました。再度操作をやり直してください。");
                }
            }).catch(error => {
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
                this.props.viewState.hideApplicationInformationSearchView();
            });

        this.draggable(document.getElementById('searchFrameDrag'), document.getElementById('searchFrame'));

    }

    /**
     * 申請者情報入力時
     * @param {number} 申請者情報の対象index
     * @param {string} 入力された値
     */
    inputChange(index, value) {
        let applicantInformation = this.state.applicantInformation;
        applicantInformation[index].value = value;
        this.setState({ applicantInformation: applicantInformation });
    }

    /**
     * 申請区分選択時①
     * @param {number} 対象index
     * @param {number} 対象画面key
     */
    handleSelectScreen(index, key) {
        let selectedScreen = this.state.selectedScreen;
        Object.keys(selectedScreen[index]).map(key => {
            selectedScreen[index][key].checked = false;
        })
        if (key >= 0) {
            selectedScreen[index][key].checked = true;
        }
        this.setState({ selectedScreen: selectedScreen });
    }

    /**
     * 申請区分選択時②
     * @param {number} 対象画面index
     * @param {number} 対象区分key
     */
    handleSelectApplicationCategory(index, categoryKey) {
        let selectedScreen = this.state.selectedScreen;
        Object.keys(selectedScreen[index]).map(key => {
            Object.keys(selectedScreen[index][key]["applicationCategory"]).map(categoryKey => {
                selectedScreen[index][key]["applicationCategory"][categoryKey].checked = false;
            })
            if (categoryKey >= 0) {
                if (selectedScreen[index][key].checked) {
                    selectedScreen[index][key]["applicationCategory"][categoryKey].checked = true;
                }
            }
        })
        this.setState({ selectedScreen: selectedScreen });
    }

    /**
     * ステータス選択時
     * @param {number} 対象index
     */
    handleSelectStatus(index) {
        let status = this.state.status;
        Object.keys(status).map(index => {
            status[index].checked = false;
        })
        if (index >= 0) {
            status[index].checked = true;
        }
        this.setState({ status: status });
    }

    /**
     * 部署選択時
     * @param {number} 対象index
     */
    handleSelectDepartment(index) {
        let department = this.state.department;
        Object.keys(department).map(index => {
            department[index].checked = false;
        })
        if (index >= 0) {
            department[index].checked = true;
        }
        this.setState({ department: department });
    }

    // クリア
    clear() {
        let status = this.state.status;
        Object.keys(status).map(index => {
            status[index].checked = false;
        })
        let department = this.state.department;
        Object.keys(department).map(index => {
            department[index].checked = false;
        })
        let selectedScreen = this.state.selectedScreen;
        Object.keys(selectedScreen).map(index => {
            Object.keys(selectedScreen[index]).map(key => {
                selectedScreen[index][key].checked = false;
                Object.keys(selectedScreen[index][key]["applicationCategory"]).map(categoryKey => {
                    selectedScreen[index][key]["applicationCategory"][categoryKey].checked = false;
                })
            })
        })
        let applicantInformation = this.state.applicantInformation;
        Object.keys(applicantInformation).map(key => {
            applicantInformation[key].value = "";
        })
        this.setState({ status: status, department: department, selectedScreen: selectedScreen, applicantInformation: applicantInformation });
    }
    
    // 検索
    search() {
        document.getElementById("searchFrame").scrollTop = 0;
        document.getElementById("customloaderSearch").style.display = "block";
        const selectedScreen = this.state.selectedScreen;
        let resultScreen = new Array();
        Object.keys(selectedScreen).map(index => {
            Object.keys(selectedScreen[index]).map(screenKey => {
                if (selectedScreen[index][screenKey].checked && selectedScreen[index][screenKey].screenId) {
                    const resultScreenIndex = resultScreen.length;
                    resultScreen[resultScreenIndex] = JSON.parse(JSON.stringify(selectedScreen[index][screenKey]));
                    resultScreen[resultScreenIndex]["applicationCategory"] = new Array();
                    Object.keys(selectedScreen[index][screenKey]["applicationCategory"]).map(categoryKey => {
                        if (selectedScreen[index][screenKey]["applicationCategory"][categoryKey].checked) {
                            resultScreen[resultScreenIndex]["applicationCategory"] = new Array(JSON.parse(JSON.stringify(selectedScreen[index][screenKey]["applicationCategory"][categoryKey])));
                        }
                    })
                }
            })
        })
        const status = this.state.status;
        let resultStatus = new Array();
        Object.keys(status).map(index => {
            if (status[index].checked) {
                resultStatus[resultStatus.length] = JSON.parse(JSON.stringify(status[index]));
            }
        })
        const department = this.state.department;
        let resultDepartment = new Array();
        Object.keys(department).map(index => {
            if (department[index].checked) {
                resultDepartment[resultDepartment.length] = JSON.parse(JSON.stringify(department[index]));
            }
        })
        const applicantInformation = this.state.applicantInformation;
        let resultApplicantInformation = new Array();
        Object.keys(applicantInformation).map(index => {
            if (applicantInformation[index].value) {
                resultApplicantInformation[resultApplicantInformation.length] = JSON.parse(JSON.stringify(applicantInformation[index]));
            }
        })
        console.log(resultScreen);
        console.log(resultStatus);
        console.log(resultDepartment);
        console.log(resultApplicantInformation);
        fetch(Config.config.apiUrl + "/application/search", {
            method: 'POST',
            body: JSON.stringify({
                applicantInformationItemForm: resultApplicantInformation,
                applicationCategories: resultScreen,
                status: resultStatus,
                department: resultDepartment
            }),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
            .then(res => res.json())
            .then(res => {
                console.log(res);
                if (Object.keys(res).length > 0 && !res.status) {
                    this.setState({
                        searchValue: res
                    });
                } else if (res.status) {
                    this.setState({ searchValue: [] });
                    alert(res.status + "エラーが発生しました");
                } else {
                    this.setState({ searchValue: [] });
                    alert("検索結果はありません");
                }
            }).catch(error => {
                this.setState({ searchValue: [] });
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            }).finally(() => document.getElementById("customloaderSearch").style.display = "none");
    }

    /**
     * 詳細画面へ遷移
     * @param {number} 申請ID
     */
    details(applicationId) {
        this.props.viewState.nextApplicationDetailsView(applicationId);
    }

    /**
     * 申請地レイヤーの表示
     * @param {object} 申請地情報
     */
    showLayers(lotNumbers) {
        try{
            const wmsUrl = Config.config.geoserverUrl;
            const items = this.state.terria.workbench.items;
            let layerFlg = false;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget) {
                    aItem.setTrait(CommonStrata.user,
                        "parameters",
                        {
                            "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationSearchTarget + Object.keys(lotNumbers)?.map(key => { return lotNumbers[key].chibanId }).filter(chibanId => { return chibanId !== null }).join("_"),
                        });
                    aItem.loadMapItems();
                    layerFlg = true;
                }
            }

            this.focusMapPlaceDriver(lotNumbers);

            if(!layerFlg){
                const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget, this.state.terria);
                item.setTrait(CommonStrata.definition, "url", wmsUrl);
                item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForApplicationSearchTarget);
                item.setTrait(
                    CommonStrata.user,
                    "layers",
                    Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget);
                item.setTrait(CommonStrata.user,
                    "parameters",
                    {
                        "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationSearchTarget + Object.keys(lotNumbers)?.map(key => { return lotNumbers[key].chibanId }).filter(chibanId => { return chibanId !== null }).join("_"),
                    });
                item.loadMapItems();
                this.state.terria.workbench.add(item);
            }
        } catch (error) {
            console.error('処理に失敗しました', error);
        }
    }

    /**
     * フォーカス処理ドライバー
     * @param {object} 申請地情報
     */
    focusMapPlaceDriver(lotNumbers) {
        let applicationPlace = Object.values(lotNumbers);
        applicationPlace = applicationPlace.filter(Boolean);
        let maxLon = 0;
        let maxLat = 0;
        let minLon = 0;
        let minLat = 0;
        Object.keys(applicationPlace).map(key => {
            const targetMaxLon = parseFloat(applicationPlace[key].maxlon);
            const targetMaxLat = parseFloat(applicationPlace[key].maxlat);
            const targetMinLon = parseFloat(applicationPlace[key].minlon);
            const targetMinLat = parseFloat(applicationPlace[key].minlat);
            if (key === 0 || key === "0") {
                maxLon = targetMaxLon;
                maxLat = targetMaxLat;
                minLon = targetMinLon;
                minLat = targetMinLat;
            } else {
                if (maxLon < targetMaxLon) {
                    maxLon = targetMaxLon;
                }
                if (maxLat < targetMaxLat) {
                    maxLat = targetMaxLat;
                }
                if (minLon > targetMinLon) {
                    minLon = targetMinLon;
                }
                if (minLat > targetMinLat) {
                    minLat = targetMinLat;
                }
            }
        })
        this.outputFocusMapPlace(maxLon, maxLat, minLon, minLat, (maxLon + minLon) / 2, (maxLat + minLat) / 2);
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
    outputFocusMapPlace(maxLon, maxLat, minLon, minLat, lon, lat) {
        // 3dmodeにセット
        this.props.viewState.set3dMode();
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
            this.state.terria.currentViewer.zoomTo(newCamera, 5);
        })
    }

    //閉じる
    close() {
        try{
            const items = this.state.terria.workbench.items;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationSearchTarget || aItem.uniqueId === Config.landMark.id) {
                    this.state.terria.workbench.remove(aItem);
                    aItem.loadMapItems();
                }
            }
        } catch (error) {
            console.error('処理に失敗しました', error);
        }
        this.props.viewState.hideApplicationInformationSearchView();
    }

    /**
     * コンポーネントドラッグ操作
     * @param {Object} ドラッグ操作対象
     * @param {Object} ドラッグ対象
     */
    draggable(target, content) {
        target.onmousedown = function () {
            document.onmousemove = mouseMove;
        };
        document.onmouseup = function () {
            document.onmousemove = null;
        };
        function mouseMove(e) {
            var event = e ? e : window.event;
            content.style.top = (event.clientY + (parseInt(content.clientHeight) / 2) - 10) + 'px';
            content.style.left = event.clientX + 'px';
        }
    }

    render() {
        const applicantInformation = this.state.applicantInformation;
        const selectedScreen = this.state.selectedScreen;
        const status = this.state.status;
        const department = this.state.department;
        const table = this.state.table;
        const searchValue = this.state.searchValue;
        return (
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                styledWidth={"700px"}
                styledHeight={"600px"}
                fullHeight
                id="searchFrame"
                onClick={() =>
                    this.props.viewState.setTopElement("ApplicationInformationSearch")}
                css={`
          position: fixed;
          z-index: 9992;
        `}
                className={CustomStyle.custom_frame}
            >
                <div id="customloaderSearch" className={CustomStyle.customloaderParent}>
                    <div className={CustomStyle.customloader}>Loading...</div>
                </div>
                <Box position="absolute" paddedRatio={3} topRight>
                    <RawButton onClick={() => {
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
                </Box>
                <nav className={CustomStyle.custom_nuv} id="searchFrameDrag">
                    申請情報検索
                </nav>
                <Box
                    centered
                    paddedHorizontally={5}
                    paddedVertically={10}
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    <Spacing bottom={1} />
                    <button
                        className={CustomStyle.close_button}
                        onClick={e => {
                            this.close();
                        }}
                    >
                        <span>戻る</span>
                    </button>
                    <Spacing bottom={2} />
                    <div className={CustomStyle.scrollContainer}>
                        <div><Text>■申請者情報</Text></div>
                        {Object.keys(applicantInformation).map(key => (
                            <div className={CustomStyle.applicant_information_box}>
                                <div className={CustomStyle.caption}>
                                    <Text>
                                        {!applicantInformation[key]?.requireFlag && (
                                            "("
                                        )}
                                        {applicantInformation[key]?.name}
                                        {!applicantInformation[key]?.requireFlag && (
                                            ")"
                                        )}</Text>
                                </div>
                                <div className={CustomStyle.input_text}>
                                    <Input
                                        light={true}
                                        dark={false}
                                        type="text"
                                        value={applicantInformation[key]?.value}
                                        placeholder=""
                                        id={applicantInformation[key]?.id}
                                        style={{ height: "28px" }}
                                        onChange={e => this.inputChange(key, e.target.value)}
                                    />
                                </div>

                            </div>
                        ))}
                        <div className={CustomStyle.clear_left}></div>
                        <div className={CustomStyle.box}>
                            <div>
                                <div><Text>■ステータス</Text></div>
                                <Select
                                    light={true}
                                    dark={false}
                                    onChange={e => this.handleSelectStatus(e.target.value)}
                                    style={{ color: "#000", width: "300px", minHeight: "28px" }}>
                                    <option value={-1}></option>
                                    {Object.keys(status).map(key => (
                                        <option key={"status" + key} value={key} selected={status[key]?.checked}>
                                            {status[key]?.text}
                                        </option>
                                    ))}
                                </Select>
                            </div>
                            <div>
                                <div><Text>■担当課</Text></div>
                                <Select
                                    light={true}
                                    dark={false}
                                    onChange={e => this.handleSelectDepartment(e.target.value)}
                                    style={{ color: "#000", width: "300px", minHeight: "28px" }}>
                                    <option value={-1}></option>
                                    {Object.keys(department).map(key => (
                                        <option key={"department" + key} value={key} selected={department[key]?.checked}>
                                            {department[key]?.departmentName}
                                        </option>
                                    ))}
                                </Select>
                            </div>
                        </div>
                        <div className={CustomStyle.clear_left}></div>
                        <div><Text>■申請区分</Text></div>
                        {Object.keys(selectedScreen).map(index => (
                            <div className={CustomStyle.box}>
                                <div>
                                    <Text>条件 {Number(index) + 1}</Text>
                                </div>
                                <div>
                                    <Select
                                        light={true}
                                        dark={false}
                                        onChange={e => this.handleSelectScreen(index, e.target.value)}
                                        style={{ color: "#000", width: "250px", minHeight: "28px" }}>
                                        <option value={-1}></option>
                                        {Object.keys(selectedScreen[index]).map(key => (
                                            <option key={index + selectedScreen[index][key]?.screenId} value={key} selected={selectedScreen[index][key]?.checked}>
                                                {selectedScreen[index][key]?.title}
                                            </option>
                                        ))}
                                    </Select>
                                </div>
                                <div>
                                    <Select
                                        light={true}
                                        dark={false}
                                        onChange={e => this.handleSelectApplicationCategory(index, e.target.value)}
                                        style={{ color: "#000", width: "250px", minHeight: "28px" }}>
                                        <option value={-1}></option>
                                        {Object.keys(selectedScreen[index]).map(key => (
                                            selectedScreen[index][key].checked && Object.keys(selectedScreen[index][key]["applicationCategory"]).map(categoryKey => (
                                                <option key={index + selectedScreen[index][key]["applicationCategory"][categoryKey]?.id} value={categoryKey} selected={selectedScreen[index][key]["applicationCategory"][categoryKey]?.checked}>
                                                    {selectedScreen[index][key]["applicationCategory"][categoryKey]?.content}
                                                </option>
                                            ))
                                        ))}
                                    </Select>
                                </div>
                            </div>
                        ))}
                        <div className={CustomStyle.clear_left}></div>
                        <Spacing bottom={2} />
                        <div className={CustomStyle.search_button_box}>
                            <div style={{ fontSize: ".7em", paddingTop: "1.8em" }}>検索結果件数：{Number(Object.keys(searchValue).length).toLocaleString()} 件</div>
                            <button
                                className={CustomStyle.clear_button}
                                onClick={e => {
                                    this.clear();
                                }}
                            >
                                <span>クリア</span>
                            </button>
                            <button
                                className={CustomStyle.search_button}
                                onClick={e => {
                                    this.search();
                                }}
                            >
                                <span>検索</span>
                            </button>
                        </div>
                        <div className={CustomStyle.clear_left}></div>
                        <Spacing bottom={2} />
                        <div>
                            {Object.keys(table).length > 0 && (
                                <table className={CustomStyle.selection_table}>
                                    <thead>
                                        <tr className={CustomStyle.table_header}>
                                            {Object.keys(table).map(key => (
                                                <th style={{ width: table[key].tableWidth + "%" }}>
                                                    {table[key]?.displayColumnName}
                                                </th>
                                            ))}
                                            <th style={{ width: "50px" }}></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {Object.keys(searchValue).map(searchKey => (
                                            <tr className={CustomStyle.tr_button} key={searchKey} onClick={() => {
                                                this.showLayers(searchValue[searchKey]?.lotNumbers);
                                            }}>
                                                {Object.keys(table).map(tableKey => (
                                                    <td style={{ width: table[tableKey].tableWidth + "%" }}>
                                                        {searchValue[searchKey]?.attributes?.[table[tableKey].resonseKey].map(text => { return text }).filter(text => { return text !== null }).join(",")}
                                                    </td>
                                                ))}
                                                <td>
                                                    <button
                                                        className={CustomStyle.detail_button}
                                                        onClick={evt => {
                                                            evt.preventDefault();
                                                            evt.stopPropagation();
                                                            this.details(searchValue[searchKey]?.applicationId);
                                                        }}
                                                    >
                                                        <span>詳細</span>
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            )}
                        </div>
                    </div>
                </Box>
            </Box >
        );
    }
}
export default withTranslation()(withTheme(ApplicationInformationSearch));