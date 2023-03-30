import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Box from "../../../../Styled/Box";
import Button, { RawButton } from "../../../../Styled/Button";
import CustomStyle from "./scss/general-condition-diagnosis.scss";
import CommonStrata from "../../../../Models/Definition/CommonStrata";
import webMapServiceCatalogItem from '../../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import Cartographic from "terriajs-cesium/Source/Core/Cartographic";
import Ellipsoid from "terriajs-cesium/Source/Core/Ellipsoid";
import {
    getShareData
} from "../../../Map/Panels/SharePanel/BuildShareLink";
import Config from "../../../../../customconfig.json";
import sampleTerrainMostDetailed from "terriajs-cesium/Source/Core/sampleTerrainMostDetailed";
/**
 * 概況診断結果画面
 */
@observer
class GeneralConditionDiagnosis extends React.Component {
    static displayName = "GeneralConditionDiagnosis";
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
            //概況診断結果
            generalConditionDiagnosisResult: [],
            //ボタン非活性フラグ
            disabledFlg: true,
            //概況診断結果レポート出力 処理対象総数
            total: 0,
            //概況診断結果レポート出力 処理対象済み総数
            result: 0,
            //概況診断結果レポート出力 capture一覧
            uploadForGeneralConditionDiagnosisForm:{}
        };
        //概況診断結果レポート出力 スクリーンショットinterval処理
        this.screenShot = null;
        //概況診断結果レポート出力 スクリーンショット完了率
        this.captureMax = 85;
        //概況診断結果レポート出力 スクリーンショット最大完了率
        this.captureAllMax = 90;
    }

    componentDidMount() {
        const { t } = this.props;
        //サーバからデータを取得
        let applicationPlace = Object.values(this.props.viewState.applicationPlace);
        applicationPlace = applicationPlace.filter(Boolean);
        let checkedApplicationCategory = Object.values(this.props.viewState.checkedApplicationCategory);
        checkedApplicationCategory = checkedApplicationCategory.filter(Boolean);
        let generalConditionDiagnosisResult = this.props.viewState.generalConditionDiagnosisResult;
        try{
            const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForApplicationTarget, this.state.terria);
            const wmsUrl = Config.config.geoserverUrl;
            const items = this.state.terria.workbench.items;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationTarget) {
                    this.state.terria.workbench.remove(aItem);
                    aItem.loadMapItems();
                }
            }
            item.setTrait(CommonStrata.definition, "url", wmsUrl);
            item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForApplicationTarget);
            item.setTrait(
                CommonStrata.user,
                "layers",
                Config.layer.lotnumberSearchLayerNameForApplicationTarget);
            item.setTrait(CommonStrata.user,
                "parameters",
                {
                    "viewparams": Config.layer.lotnumberSearchViewParamNameForApplicationTarget + Object.keys(this.props.viewState.applicationPlace)?.map(key => { return this.props.viewState.applicationPlace[key].chibanId }).filter(chibanId => { return chibanId !== null }).join("_"),
                });
            item.loadMapItems();
            this.state.terria.workbench.add(item);
            this.focusMapPlaceDriver();
        }catch(error){
            console.error('処理に失敗しました', error);
        }
        if(generalConditionDiagnosisResult && Object.keys(generalConditionDiagnosisResult).length > 0){
            let disabledFlg = this.state.disabledFlg;
            Object.keys(generalConditionDiagnosisResult).map(key => {
                if (generalConditionDiagnosisResult[key].result) {
                    disabledFlg = false;
                }
            });
            this.setState({ generalConditionDiagnosisResult: generalConditionDiagnosisResult, disabledFlg: disabledFlg });
        }else{
            fetch(Config.config.apiUrl + "/judgement/execute", {
                method: 'POST',
                body: JSON.stringify({
                    lotNumbers: applicationPlace,
                    applicationCategories: checkedApplicationCategory
                }),
                headers: new Headers({ 'Content-type': 'application/json' }),
            })
                .then(res => res.json())
                .then(res => {
                    let disabledFlg = this.state.disabledFlg;
                    if (Object.keys(res).length > 0 && !res.status) {
                        Object.keys(res).map(key => {
                            if (res[key].result) {
                                disabledFlg = false;
                            }
                        });
                        this.setState({ generalConditionDiagnosisResult: res, disabledFlg: disabledFlg });
                    } else {
                        this.setState({ generalConditionDiagnosisResult: [], disabledFlg: true });
                        alert("概況診断に失敗しました。\n再度初めからやり直してください。");
                    }
                }).catch(error => {
                    this.setState({ generalConditionDiagnosisResult: [], disabledFlg: true });
                    console.error('処理に失敗しました', error);
                    alert('処理に失敗しました');
                });
        }
        this.draggable(document.getElementById('GeneralConditionDiagnosisDrag'), document.getElementById('GeneralConditionDiagnosis'));

    }

    //キャプチャを作成し、概況診断結果レポートを出力
    outputPreparation() {
        if (window.confirm("現在マップで表示されている領域に申請地が全て含まれるようにしてください。このままレポートの出力を開始しますか？")) {
            let myBar = document.getElementById("myBar");
            let wholePercent = document.getElementById("wholePercent");
            document.getElementById("loadingBg").style.display = "block";
            document.getElementById("loading").style.display = "block";
            //完了率をスクリーンショット最大完了率 - スクリーンショット完了率にしてレイヤ表示切替処理に遷移
            myBar.style.width = this.captureAllMax - this.captureMax + "%";
            wholePercent.innerHTML = this.captureAllMax - this.captureMax;
            try{
                this.showCaptureLayers();
            }catch(error){
                this.errorHandler(error);
            }
        }
    }

    //キャプチャを作成し、概況診断結果レポートを出力（申請登録時）
    outputPreparationForConfirmApplicationDetails() {
        if (window.confirm("申請に必要なレポートの生成を行います。現在マップで表示されている領域に申請地が全て含まれるようにしてください。このまま申請処理を開始しますか？")) {
            let myBar = document.getElementById("myBar");
            let wholePercent = document.getElementById("wholePercent");
            document.getElementById("loadingBg").style.display = "block";
            document.getElementById("loading").style.display = "block";
            //完了率をスクリーンショット最大完了率 - スクリーンショット完了率にしてレイヤ表示切替処理に遷移
            myBar.style.width = this.captureAllMax - this.captureMax + "%";
            wholePercent.innerHTML = this.captureAllMax - this.captureMax;
            try{
                this.showCaptureLayers();
            }catch(error){
                this.errorHandler(error);
            }
        }
    }

    /**
     * フォーカス処理ドライバー
     */
    focusMapPlaceDriver() {
        let applicationPlace = this.props.viewState.applicationPlace;
        applicationPlace = Object.values(applicationPlace);
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

    /**
     * レイヤ表示切替及びcapture処理（呼び出し元）
     */
    showCaptureLayers() {
        let numberOfSheets = document.getElementById("numberOfSheets");
        let myBar = document.getElementById("myBar");
        let wholePercent = document.getElementById("wholePercent");
        let total = 1;
        let result = 0;
        let max = this.captureMax;
        let uploadForGeneralConditionDiagnosisForm = {};
        let generalConditionDiagnosisResult = this.state.generalConditionDiagnosisResult;
        generalConditionDiagnosisResult = Object.values(generalConditionDiagnosisResult);
        generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);

        //全ての処理数を算出
        Object.keys(generalConditionDiagnosisResult).map((key) => {
            if (Object.keys(generalConditionDiagnosisResult[key].layers).length > 0) {
                total = total + 1;
            }
        });
        numberOfSheets.innerHTML = result + "/" + total;

        //概況図のcaptureを取得するために関連レイヤのリセット
        Object.keys(generalConditionDiagnosisResult).map(key => {
            const items = this.state.terria.workbench.items;
            const layers = generalConditionDiagnosisResult[key].layers;
            for (const aItem of items) {
                Object.keys(layers).map(key => {
                    if (aItem.uniqueId === layers[key].layerCode || aItem.uniqueId === '対象地点') {
                        this.state.terria.workbench.remove(aItem);
                        aItem.loadMapItems();
                    }
                });
            }
        });

        clearInterval(this.screenShot);
        this.screenShot = setInterval(() => {
            if(parseInt(document.getElementById("ProgressBarJsx").style.width) >= 80){
                clearInterval(this.screenShot);
                this.state.terria.currentViewer
                .captureScreenshot()
                .then(dataString => {
                    uploadForGeneralConditionDiagnosisForm[result] = {"image":this.decodeBase64(dataString),"judgementId":null,"currentSituationMapFlg":true};
                    myBar.style.width = parseInt(parseInt(myBar.style.width) + (max / total)) + "%";
                    result = result + 1;
                    numberOfSheets.innerHTML = result + "/" + total;
                    wholePercent.innerHTML = parseInt(myBar.style.width);
                    //設定値の初期化
                    this.setState({total:total,result:result,uploadForGeneralConditionDiagnosisForm:uploadForGeneralConditionDiagnosisForm});
                    //概況図のcaptureを取得後、区分判定毎にレイヤの表示切替を行いcaptureを取得
                    this.generalConditionDiagnosisResultLoop(0);
                }).catch(error => {
                    this.errorHandler(error);
                });
            }
        }, 3000);

    }

    /**
     * 概況診断capture取得処理(再帰処理)
     * @param {number} 概況診断結果の処理対象index
     */
    generalConditionDiagnosisResultLoop(index){
        let generalConditionDiagnosisResult = this.state.generalConditionDiagnosisResult;
        generalConditionDiagnosisResult = Object.values(generalConditionDiagnosisResult);
        generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);
        if (Object.keys(generalConditionDiagnosisResult[index].layers).length > 0) {
            //概況図のcaptureを取得するために関連レイヤのリセット
            Object.keys(generalConditionDiagnosisResult).map(innerKey => {
                const innerItems = this.state.terria.workbench.items;
                const innerLayers = generalConditionDiagnosisResult[innerKey].layers;
                for (const aItem of innerItems) {
                    Object.keys(innerLayers).map(innerLayerKey => {
                        if (aItem.uniqueId === innerLayers[innerLayerKey].layerCode) {
                            this.state.terria.workbench.remove(aItem);
                            aItem.loadMapItems();
                        }
                    });
                }
            });
            this.layerLoop(index,0);
        }else{
            const result = this.state.result;
            const total = this.state.total;
            if(result === total){
                this.outputFile(this.state.uploadForGeneralConditionDiagnosisForm);
            }else if(index < Object.keys(generalConditionDiagnosisResult).length-1){
                index = index + 1;
                this.generalConditionDiagnosisResultLoop(index);
            }
        }
    }

    /**
     * レイヤー表示処理(再帰処理)
     * @param {number} 概況診断結果の処理対象index
     * @param {number} 対象レイヤ一覧の処理対象index
     */
    layerLoop(index,layerIndex){
        let generalConditionDiagnosisResult = this.state.generalConditionDiagnosisResult;
        generalConditionDiagnosisResult = Object.values(generalConditionDiagnosisResult);
        generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);
        let layers = generalConditionDiagnosisResult[index].layers;
        layers = Object.values(layers);
        layers = layers.filter(Boolean);
        // 判定レイヤと同時表示レイヤを表示
        if ((generalConditionDiagnosisResult[index].result && layers[layerIndex].layerType) || (generalConditionDiagnosisResult[index].judgementLayerDisplayFlag && layers[layerIndex].layerType) || (generalConditionDiagnosisResult[index].simultameousLayerDisplayFlag && !layers[layerIndex].layerType)) {
            const item = new webMapServiceCatalogItem(layers[layerIndex].layerCode, this.state.terria);
            const wmsUrl = Config.config.geoserverUrl;
            item.setTrait(CommonStrata.definition, "url", wmsUrl);
            item.setTrait(CommonStrata.user, "name", layers[layerIndex].layerName);
            item.setTrait(
                CommonStrata.user,
                "layers",
                layers[layerIndex].layerCode);
            if (layers[layerIndex].layerQuery && layers[layerIndex].queryRequireFlag) {
                item.setTrait(CommonStrata.user,
                    "parameters",
                    {
                        "viewparams": layers[layerIndex].layerQuery,
                    });
            } else {
                item.setTrait(CommonStrata.user,
                    "parameters",
                    {});
            }
            item.loadMapItems();
            this.state.terria.workbench.add(item).then(
                r =>{
                    this.layerChange(index,layerIndex);
                }
            );
        }else{
            this.layerChange(index,layerIndex);
        }
    }

    /**
     * レイヤーcapture取得及び判定処理
     * @param {number} 概況診断結果の処理対象index
     * @param {number} 対象レイヤ一覧の処理対象index
     */
     layerChange(index,layerIndex){
        let generalConditionDiagnosisResult = this.state.generalConditionDiagnosisResult;
        generalConditionDiagnosisResult = Object.values(generalConditionDiagnosisResult);
        generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);
        let layers = generalConditionDiagnosisResult[index].layers;
        layers = Object.values(layers);
        layers = layers.filter(Boolean);
        let uploadForGeneralConditionDiagnosisForm = this.state.uploadForGeneralConditionDiagnosisForm;
        let myBar = document.getElementById("myBar");
        let numberOfSheets = document.getElementById("numberOfSheets");
        let wholePercent = document.getElementById("wholePercent");
        let max = this.captureMax;
        let allMax = this.captureAllMax;

        if(layerIndex === Object.keys(layers).length - 1){
            clearInterval(this.screenShot);
            this.screenShot = setInterval(() => {
                let result = this.state.result;
                let total = this.state.total;
                if(parseInt(document.getElementById("ProgressBarJsx").style.width) >= 80){
                    clearInterval(this.screenShot);
                    this.state.terria.currentViewer
                    .captureScreenshot()
                    .then(dataString => {
                        uploadForGeneralConditionDiagnosisForm[result] = { "image": this.decodeBase64(dataString), "judgementId": generalConditionDiagnosisResult[index].judgementId, "currentSituationMapFlg": false };
                        result = result + 1;
                        if (total > result ) {
                            myBar.style.width = parseInt(parseInt(myBar.style.width) + (max / total)) + "%";
                            numberOfSheets.innerHTML = result + "/" + total;
                            wholePercent.innerHTML = parseInt(myBar.style.width);
                            index = index + 1;
                            this.setState({result:result,total:total,uploadForGeneralConditionDiagnosisForm:uploadForGeneralConditionDiagnosisForm});
                            this.generalConditionDiagnosisResultLoop(index);
                        } else if(result === total){
                            myBar.style.width = allMax + "%";
                            numberOfSheets.innerHTML = result + "/" + total;
                            wholePercent.innerHTML = allMax;
                            this.setState({result:result,total:total,uploadForGeneralConditionDiagnosisForm:uploadForGeneralConditionDiagnosisForm});
                            this.outputFile(uploadForGeneralConditionDiagnosisForm);
                        }
                    }).catch(error => {
                        this.errorHandler(error);
                    });
                }
            }, 3000);
        }else{
            layerIndex = layerIndex + 1;
            this.layerLoop(index,layerIndex);
        }
     }

    /**
     * base64形式からfileObjectへ変換
     * @param {string} base64形式
     * @return {File} fileObject
     */
    decodeBase64(fileData) {
        let bin = atob(fileData.replace(/^.*,/, ''));
        let buffer = new Uint8Array(bin.length);
        for (let i = 0; i < bin.length; i++) {
            buffer[i] = bin.charCodeAt(i);
        }
        let image_file = new File([buffer.buffer], "map.png", { type: "image/png" });
        return image_file;
    }

    /**
     * 概況診断レポートをExcel形式で出力
     * @param {Object} 概況診断結果のcapture一覧
     */
    outputFile(uploadForGeneralConditionDiagnosisForm) {
        let generalConditionDiagnosisResult = this.state.generalConditionDiagnosisResult;
        generalConditionDiagnosisResult = Object.values(generalConditionDiagnosisResult);
        generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);
        let applicationPlace = this.props.viewState.applicationPlace;
        applicationPlace = Object.values(applicationPlace);
        applicationPlace = applicationPlace.filter(Boolean);
        let checkedApplicationCategory = this.props.viewState.checkedApplicationCategory;
        checkedApplicationCategory = Object.values(checkedApplicationCategory);
        checkedApplicationCategory = checkedApplicationCategory.filter(Boolean);
        let myBar = document.getElementById("myBar");
        let wholePercent = document.getElementById("wholePercent");
        let numberOfSheets = document.getElementById("numberOfSheets");
        let max = 100-this.captureAllMax-2;
        let allMax = 100;
        fetch(Config.config.apiUrl + "/judgement/image/upload/preparation")
            .then(res => res.json())
            .then(res => {
                if (res.folderName) {
                    const folderName = res.folderName;
                    this.props.viewState.setFolderName(folderName);
                    Object.keys(uploadForGeneralConditionDiagnosisForm).map(key => {
                        uploadForGeneralConditionDiagnosisForm[key].folderName = folderName;
                        const formData = new FormData();
                        for (const name in uploadForGeneralConditionDiagnosisForm[key]) {
                            formData.append(name, uploadForGeneralConditionDiagnosisForm[key][name]);
                        }
                        fetch(Config.config.apiUrl + "/judgement/image/upload", {
                            method: 'POST',
                            body: formData,
                        })
                            .then(res => res.json())
                            .then(res => {
                                myBar.style.width = parseInt(parseInt(myBar.style.width) + (max / Object.keys(uploadForGeneralConditionDiagnosisForm).length)) + "%";
                                wholePercent.innerHTML = parseInt(myBar.style.width);
                                uploadForGeneralConditionDiagnosisForm[key]["status"] = res.status;
                                let completeFlg = true;
                                Object.keys(uploadForGeneralConditionDiagnosisForm).map(key => {
                                    if (!uploadForGeneralConditionDiagnosisForm[key]["status"]) {
                                        completeFlg = false;
                                    }
                                })
                                if (completeFlg) {
                                    //申請登録時のレポート生成の場合帳票出力は行わない
                                    if(!document.getElementById("confirmApplicationDetailsRegisterButton")){
                                        fetch(Config.config.apiUrl + "/judgement/report", {
                                            method: 'POST',
                                            body: JSON.stringify({
                                                folderName: folderName,
                                                lotNumbers: applicationPlace,
                                                applicationCategories: checkedApplicationCategory,
                                                generalConditionDiagnosisResults: generalConditionDiagnosisResult
                                            }),
                                            headers: new Headers({ 'Content-type': 'application/json' }),
                                        })
                                            .then((res) => res.blob())
                                            .then(blob => {
                                                myBar.style.width = allMax + "%";
                                                wholePercent.innerHTML = parseInt(myBar.style.width);
                                                const now = new Date();
                                                const filename = "概況診断結果" + now.toLocaleDateString();
                                                let anchor = document.createElement("a");
                                                anchor.href = window.URL.createObjectURL(blob);
                                                anchor.download = filename + ".xlsx";
                                                anchor.click();
                                                try{
                                                    Object.keys(generalConditionDiagnosisResult).map(key => {
                                                        const items = this.state.terria.workbench.items;
                                                        const layers = generalConditionDiagnosisResult[key].layers;
                                                        for (const aItem of items) {
                                                            Object.keys(layers).map(key => {
                                                                if (aItem.uniqueId === layers[key].layerCode) {
                                                                    this.state.terria.workbench.remove(aItem);
                                                                    aItem.loadMapItems();
                                                                }
                                                            });
                                                        }
                                                    });
                                                }catch(error){
                                                    console.error('処理に失敗しました', error);
                                                }
                                            })
                                            .catch(error => {
                                                alert('レポートの生成に失敗しました');
                                            }).finally(() => {
                                                setTimeout(() => {
                                                    document.getElementById("loadingBg").style.display = "none";
                                                    document.getElementById("loading").style.display = "none";
                                                    myBar.style.width = "0%";
                                                    wholePercent.innerHTML = 0;
                                                    numberOfSheets.innerHTML = 0 + "/" + 0;
                                                }, 3000)
                                            });
                                        }else{
                                            this.clearLayer();
                                            document.getElementById("loadingBg").style.display = "none";
                                            document.getElementById("loading").style.display = "none";
                                            myBar.style.width = "0%";
                                            wholePercent.innerHTML = 0;
                                            numberOfSheets.innerHTML = 0 + "/" + 0;
                                            document.getElementById("confirmApplicationDetailsRegisterButton").click();
                                        }
                                }
                            }).catch(error => {
                                console.error('処理に失敗しました', error);
                            });
                    });
                } else {
                    this.errorHandler(null);
                    alert('一時フォルダの生成に失敗しました');
                }
            }).catch(error => {
                this.errorHandler(error);
                alert('通信処理に失敗しました');
            });
    }

    /**
     * 関連レイヤの表示切替処理
     * @param {Object} obj 対象の概況診断結果
     */
    showLayers(obj) {
        try{
            const items = this.state.terria.workbench.items;
            const layers = obj.layers;
            let generalConditionDiagnosisResult = this.state.generalConditionDiagnosisResult;
            // 一旦全ての関連レイヤをリセット
            Object.keys(generalConditionDiagnosisResult).map(key => {
                const layers = generalConditionDiagnosisResult[key].layers;
                for (const aItem of items) {
                    Object.keys(layers).map(key => {
                        if (aItem.uniqueId === layers[key].layerCode) {
                            this.state.terria.workbench.remove(aItem);
                            aItem.loadMapItems();
                        }
                    });
                }
            });
            // レイヤの表示
            Object.keys(layers).map(key => {
                // 判定レイヤと同時表示レイヤを表示
                if ((obj.result && layers[key].layerType) || (obj.judgementLayerDisplayFlag && layers[key].layerType) || (obj.simultameousLayerDisplayFlag && !layers[key].layerType)) {
                    const item = new webMapServiceCatalogItem(layers[key].layerCode, this.state.terria);
                    const wmsUrl = Config.config.geoserverUrl;
                    item.setTrait(CommonStrata.definition, "url", wmsUrl);
                    item.setTrait(CommonStrata.user, "name", layers[key].layerName);
                    item.setTrait(
                        CommonStrata.user,
                        "layers",
                        layers[key].layerCode);
                    if (layers[key].layerQuery && layers[key].queryRequireFlag) {
                        item.setTrait(CommonStrata.user,
                            "parameters",
                            {
                                "viewparams": layers[key].layerQuery,
                            });
                    } else {
                        item.setTrait(CommonStrata.user,
                            "parameters",
                            {});
                    }
                    item.loadMapItems();
                    this.state.terria.workbench.add(item);
                }
            });
        }catch(error){
            console.error('処理に失敗しました', error);
        }
    }

    // 全ての関連レイヤ(地番含む)をリセット
    clearLayer() {
        try{
            const items = this.state.terria.workbench.items;
            let generalConditionDiagnosisResult = this.state.generalConditionDiagnosisResult;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForBusiness || aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForGoverment || aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationTarget || aItem.uniqueId === Config.landMark.id) {
                    this.state.terria.workbench.remove(aItem);
                    aItem.loadMapItems();
                }
            }
            Object.keys(generalConditionDiagnosisResult).map(key => {
                const layers = generalConditionDiagnosisResult[key].layers;
                for (const aItem of items) {
                    Object.keys(layers).map(key => {
                        if (aItem.uniqueId === layers[key].layerCode) {
                            this.state.terria.workbench.remove(aItem);
                            aItem.loadMapItems();
                        }
                    });
                }
            });
        }catch(error){
            console.error('処理に失敗しました', error);
        }
    }

    //閉じる
    close() {
        this.clearLayer();
        this.props.viewState.hideGeneralConditionDiagnosisView();
    }

    //次へ
    next() {
        this.clearLayer();
        this.props.viewState.nextEnterApplicantInformationView(this.state.generalConditionDiagnosisResult);
    }

    /**
     * エラーハンドリング処理
     * @param {error}
     */
    errorHandler(error){
        document.getElementById("myBar").style.width = "0%";
        document.getElementById("wholePercent").innerHTML = 0;
        document.getElementById("numberOfSheets").innerHTML = 0 + "/" + 0;
        document.getElementById("loadingBg").style.display = "none";
        document.getElementById("loading").style.display = "none";
        if(error)
            console.error('処理に失敗しました', error);
        alert("帳票生成または帳票出力時に何らかのエラーが発生しました。大変お手数ですが一度画面を閉じて初めからやり直すか、もう一度実行してください。")
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

    /**
     * リンク文字列変換処理
     * @param {string} リンク変換前文字列
     * @return {string} リンク変換後文字列
     */
    autoLink(str) {
        let regexp_url = /((<a>h?)(ttps?:\/\/[a-zA-Z0-9.\-_@:/~?%&;=+#',()*!]+)(<\/a>))/g; // ']))/;
        let regexp_makeLink = function(all, url, h, href) {
            url = url.replace("<a>", "");
            url = url.replace("</a>", "");
            return '<a href="h' + href + '" target="_blank">' + url + '</a>';
        }
        return str.replace(regexp_url, regexp_makeLink);
    }

    render() {
        let generalConditionDiagnosisResult = this.state.generalConditionDiagnosisResult;
        let disabledFlg = this.state.disabledFlg;
        return (
            <>
                <div className={CustomStyle.loadingBg} id="loadingBg"></div>
                <div className={CustomStyle.loading} id="loading">
                    <p style={{ textAlign: "center" }}>処理中です。暫く画面はこのままでお待ちください。</p>
                    <p style={{ textAlign: "center" }}>※バックグラウンド動作の場合正常にキャプチャの切替が行われませんのでご注意ください。</p>
                    <p style={{ textAlign: "center" }}>画面キャプチャ取得中 <span id="numberOfSheets">0/0</span></p>
                    <div className={CustomStyle.myProgress}>
                        <div className={CustomStyle.myBar} id="myBar"></div>
                    </div>
                    <p style={{ textAlign: "center" }}>帳票作成中 <span id="wholePercent">0</span>%</p>
                </div>
                <Box
                    displayInlineBlock
                    backgroundColor={this.props.theme.textLight}
                    styledWidth={"450px"}
                    styledHeight={"410px"}
                    fullHeight
                    overflow={"auto"}
                    id="GeneralConditionDiagnosis"
                    css={`
          position: fixed;
          z-index: 9992;
        `}
                    className={CustomStyle.custom_frame}
                >
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
                    <nav className={CustomStyle.custom_nuv} id="GeneralConditionDiagnosisDrag">
                        概況診断結果
                    </nav>
                    <div>
                        <div className={CustomStyle.table_frame}>
                            <table className={CustomStyle.selection_table}>
                                <thead>
                                    <tr>
                                        <th style={{ width: 245 + "px" }}>対象</th>
                                        <th style={{ width: 150 + "px" }}>判定結果</th>
                                        <th style={{ width: 50 + "px" }}>距離</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {Object.keys(generalConditionDiagnosisResult).map(key => (
                                        <tr className={CustomStyle.tr_button} key={key} onClick={() => {
                                            this.showLayers(generalConditionDiagnosisResult[key]);
                                        }}>
                                            <td className={CustomStyle.title}>
                                                {generalConditionDiagnosisResult[key].title}
                                            </td>
                                            <td className={CustomStyle.result}>
                                                {generalConditionDiagnosisResult[key].summary}
                                                {generalConditionDiagnosisResult[key].description && (
                                                    <div className={CustomStyle.description}>
                                                        <div className={CustomStyle.descriptionInner}>
                                                            <span dangerouslySetInnerHTML={{ __html: this.autoLink(generalConditionDiagnosisResult[key].description) }}></span>
                                                        </div>
                                                    </div>
                                                )}
                                            </td>
                                            <td className={CustomStyle.title}>
                                                {generalConditionDiagnosisResult[key].distance}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div className={CustomStyle.custom_footer}>
                        <div className={CustomStyle.box}>
                            <div className={CustomStyle.item}>
                                <button className={CustomStyle.custom_button} disabled={disabledFlg} onClick={() => {
                                    this.outputPreparation();
                                }}>出力</button>
                                <button className={CustomStyle.custom_button} id="generalConditionOutputBtn" style={{ display: "none" }} onClick={() => {
                                    this.outputPreparationForConfirmApplicationDetails();
                                }}>出力</button>
                            </div>
                            <div className={CustomStyle.item}>
                                <button className={CustomStyle.custom_button} disabled={disabledFlg} onClick={() => {
                                    this.next();
                                }}>申請</button>
                            </div>
                            <div className={CustomStyle.item}>
                                <button className={CustomStyle.custom_button} onClick={() => {
                                    this.close();
                                }}>閉じる</button>
                            </div>
                        </div>
                    </div>
                </Box >
            </>
        );
    }
}
export default withTranslation()(withTheme(GeneralConditionDiagnosis));