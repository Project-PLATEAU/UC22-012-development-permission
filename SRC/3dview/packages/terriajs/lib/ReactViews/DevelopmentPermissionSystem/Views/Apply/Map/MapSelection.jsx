import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../../Styled/Icon";
import Box from "../../../../../Styled/Box";
import CustomStyle from "./scss/map-selection.scss";
import CommonStrata from "../../../../../Models/Definition/CommonStrata";
import webMapServiceCatalogItem from '../../../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import CzmlCatalogItem from '../../../../../Models/Catalog/CatalogItems/CzmlCatalogItem';
import Config from "../../../../../../customconfig.json";
import { BaseModel } from "../../../../../Models/Definition/Model";
/**
 * 地図選択画面
 */
@observer
class MapSelection extends React.Component {
    static displayName = "MapSelection";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        applicationPlace: PropTypes.object,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired
    }

    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria,
            //地番情報のテーブル定義
            table: []
        };
    }

    //初期描画
    componentDidMount() {
        //サーバからデータを取得
        fetch(Config.config.apiUrl + "/lotnumber/columns")
        .then(res => res.json())
        .then(res => {
            if (Object.keys(res).length > 0) {
                this.setState({table: res});
            }else{
                alert("地番情報のテーブル定義の取得に失敗しました。再度操作をやり直してください。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });

        //選択状態の申請地layerを更新
        this.updateMapLayer();
        
        try {
            //筆界layerを表示
            const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForAll, this.state.terria);
            const wmsUrl = Config.config.geoserverUrl;
            const items = this.state.terria.workbench.items;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForAll || aItem.uniqueId === Config.landMark.id) {
                    this.state.terria.workbench.remove(aItem);
                }
            }
            item.setTrait(CommonStrata.definition, "url", wmsUrl);
            item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForAll);
            item.setTrait(
                CommonStrata.user,
                "layers",
                Config.layer.lotnumberSearchLayerNameForAll);
            item.setTrait(CommonStrata.user,
                "parameters",
                {});
            item.loadMapItems();
            this.state.terria.workbench.add(item);

            //landmark layerを表示
            const landmarkUrl = Config.config.landmarkUrl;
            const catalogItem = new CzmlCatalogItem(Config.landMark.id, this.state.terria)
            catalogItem.setTrait(CommonStrata.definition, "url", landmarkUrl);
            catalogItem.setTrait(CommonStrata.user, "name", Config.landMark.displayName);
            catalogItem.loadMapItems();
            this.state.terria.workbench.add(catalogItem);

            //3D建物モデルを非表示
            const cesium3DTiles = this.state.terria.getModelById(BaseModel, Config.buildingModel.id);
            cesium3DTiles.traits.style.show = false;
            cesium3DTiles.setTrait(
                CommonStrata.user,
                "style",
                {"show": false});
            cesium3DTiles.loadMapItems();
        } catch (error) {
            console.error('処理に失敗しました', error);
        }

        this.draggable(document.getElementById('MapSelectionDrag'),document.getElementById('MapSelection'));
    }

    //選択状態の申請地layerを更新
    updateMapLayer(){
        try {
            const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForSelected, this.state.terria);
            const wmsUrl = Config.config.geoserverUrl;
            const items = this.state.terria.workbench.items;
            let initFlg = true;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForSelected && Object.keys(this.props.viewState.applicationPlace).length > 0) {
                    aItem.setTrait(CommonStrata.user,
                    "parameters",
                    {
                        "viewparams": Config.layer.lotnumberSearchViewParamNameForSelected + Object.keys(this.props.viewState.applicationPlace)?.map(key => { return this.props.viewState.applicationPlace[key].chibanId }).filter(chibanId => {return chibanId !== null}).join("_"),
                    });
                    aItem.loadMapItems();
                    initFlg = false;
                }else if(aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForSelected){
                    this.state.terria.workbench.remove(aItem);
                }
            }
            if(initFlg && Object.keys(this.props.viewState.applicationPlace).length > 0){
                item.setTrait(CommonStrata.definition, "url", wmsUrl);
                item.setTrait(CommonStrata.user, "name", Config.layer.lotnumberSearchLayerDisplayNameForSelected);
                item.setTrait(
                    CommonStrata.user,
                    "layers",
                    Config.layer.lotnumberSearchLayerNameForSelected);
                item.setTrait(CommonStrata.user,
                    "parameters",
                    {
                        "viewparams": Config.layer.lotnumberSearchViewParamNameForSelected + Object.keys(this.props.viewState.applicationPlace)?.map(key => { return this.props.viewState.applicationPlace[key].chibanId }).filter(chibanId => {return chibanId !== null}).join("_"),
                    });
                item.loadMapItems();
                this.state.terria.workbench.add(item);
            }
        } catch (error) {
            console.error('処理に失敗しました', error);
        }
    }

    /**
     * 選択状態の申請地を削除
     * @param {number} 削除対象のindex
     */
    deleteApplicationPlace(index){
        this.props.viewState.deleteApplicationPlace(index);
        this.updateMapLayer();
    }

    //概況診断へ遷移
    checkNextGeneralConditionDiagnosisView(){
        if (Object.keys(this.props.viewState.applicationPlace).length < 1) {
            alert("申請地は一つ以上必要です");
            return false;
        }
        const items = this.state.terria.workbench.items;
        for (const aItem of items) {
            if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForSelected || aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForAll) {
                this.state.terria.workbench.remove(aItem);
                aItem.loadMapItems();
            }
        }
        try{
            //3D建物モデルを表示
            const cesium3DTiles = this.state.terria.getModelById(BaseModel, Config.buildingModel.id);
            cesium3DTiles.traits.style.show = false;
            cesium3DTiles.setTrait(
                CommonStrata.user,
                "style",
                {"show": true});
            cesium3DTiles.loadMapItems();
        }catch(error){
            console.error('処理に失敗しました', error);
        }

        this.clearTooltips();
        this.props.viewState.nextGeneralConditionDiagnosisView();
    }

    //地図選択の取得処理
    clickMapSelection(){
        if(this.state.terria.clickMode === "1" && !this.state.terria.authorityJudgment()){
            fetch(Config.config.apiUrl + "/lotnumber/getFromLonlat/establishment", {
                method: 'POST',
                body: JSON.stringify({
                    latiude: this.state.terria.lat,
                    longitude:this.state.terria.lon
                }),
                headers: new Headers({ 'Content-type': 'application/json' }),
            })
            .then(res => res.json())
            .then(res => {
                if(Object.keys(res).length > 0 && !res.status){
                    //選択申請地対象を初期化
                    this.props.viewState.removeLosNumberSelect();

                    Object.keys(res).map((key) => {
                        if(!this.props.viewState.deleteApplicationPlaceByChiban(res[key]))
                            this.props.viewState.switchLotNumberSelect(res[key]); 
                    })
                    this.props.viewState.changeApplicationPlace();
                    this.updateMapLayer();
                }else{
                    alert('地点の取得に失敗しました');  
                }
            }).catch(error => {
                console.error('処理に失敗しました', error);
            });
        }
    }

    //自由選択の取得処理
    drawingMapSelection(){
        this.state.terria.setActiveShapeLonLat(this.state.terria.activeShapeLonLat[0]);
        if(this.state.terria.activeShapeLonLat.length < 3){
            return false;
        }
        if(this.state.terria.clickMode === "1" && !this.state.terria.authorityJudgment()){
            fetch(Config.config.apiUrl + "/lotnumber/getFromFigure/establishment", {
                method: 'POST',
                body: JSON.stringify({
                    coodinates: this.state.terria.activeShapeLonLat
                }),
                headers: new Headers({ 'Content-type': 'application/json' }),
            })
            .then(res => res.json())
            .then(res => {
                if(Object.keys(res).length > 0 && !res.status){
                    //選択申請地対象を初期化
                    this.props.viewState.removeLosNumberSelect();

                    Object.keys(res).map((key) => {
                        this.props.viewState.switchLotNumberSelect(res[key]); 
                    })
                    this.props.viewState.changeApplicationPlace();
                    this.updateMapLayer();
                    this.props.terria.userDrawing?.endDrawing();
                    this.props.terria.initActiveShapeLonLat();
                    this.props.terria.initActiveShapePoints();
                }else if(res.status === 406){
                    alert('選択範囲には500件以上含まれるため取得できません。\n選択範囲を限定して操作をやり直してください。');
                }
            }).catch(error => {
                console.error('処理に失敗しました', error);
            });
        }
    }

    //戻る
    back(){
        const items = this.state.terria.workbench.items;
        for (const aItem of items) {
            if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForSelected || aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForAll || aItem.uniqueId === Config.landMark.id) {
                this.state.terria.workbench.remove(aItem);
                aItem.loadMapItems();
            }
        }
        try{
            //3D建物モデルを表示
            const cesium3DTiles = this.state.terria.getModelById(BaseModel, Config.buildingModel.id);
            cesium3DTiles.traits.style.show = false;
            cesium3DTiles.setTrait(
                CommonStrata.user,
                "style",
                {"show": true});
            cesium3DTiles.loadMapItems();
        }catch(error){
            console.error('処理に失敗しました', error);
        }
        this.clearTooltips();
        this.props.viewState.backApplicationCategorySelectionView();
    }

    //ツールチップ
    tooltips() {
        document.getElementsByTagName("canvas")[0].onmousemove = mouseMove;
        document.getElementsByTagName("canvas")[0].onmouseout = mouseOut;
        function mouseMove(e) {
            var event = e ? e : window.event;
            var div = document.getElementById("tooltips");
            div.style.left = event.clientX+10 + "px";
            div.style.top = event.clientY-70 + "px";
            div.style.display = "block";
        }
        function mouseOut(e) {
            var event = e ? e : window.event;
            var div = document.getElementById("tooltips");
            div.style.display = "none";
        }
    }

    //ツールチップクリア
    clearTooltips(){
        document.getElementsByTagName("canvas")[0].onmousemove = null;
        document.getElementsByTagName("canvas")[0].onmouseout = null;
        this.props.terria.userDrawing?.endDrawing();
        this.props.terria.initActiveShapeLonLat();
        this.props.terria.initActiveShapePoints();
    }
    
    /**
     * コンポーネントドラッグ操作
     * @param {Object} ドラッグ操作対象
     * @param {Object} ドラッグ対象
     */
    draggable(target,content) {
        target.onmousedown = function(e) {
            if(e.target.id !== "shindanBtn" && e.target.id !=="mojiBtn" && e.target.id !=="mapBtn" && e.target.id !=="backBtn" && e.target.id!=="freeBtn"){
                document.onmousemove = mouseMove;
            }
        };
        document.onmouseup = function() {
            document.onmousemove = null;
        };
        function mouseMove(e) {
            var event = e ? e : window.event;
            content.style.top = (event.clientY + (parseInt(content.clientHeight)/2) - 10) + 'px';
            content.style.left = event.clientX + 'px';
        }
    }

    render() {
        const applicationPlace = this.props.viewState.applicationPlace;
        const table = this.state.table;
        return (
            <>
            <div className={CustomStyle.tooltips} id="tooltips">
                左クリック = クリック地点の地番を取得<br/>
                Alt + 左クリック = 自由形選択開始<br/>
                Alt + ドラッグ = 選択範囲描画<br/>
                Alt + ドロップ= 選択範囲の地番取得<br/>
                ※選択範囲に完全に含む地番を取得します
            </div>
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                styledWidth={"400px"}
                styledHeight={"500px"}
                fullHeight
                overflow={"auto"}
                onClick={() =>
                    this.props.viewState.setTopElement("MapSelection")}
                css={`
          position: fixed;
          z-index: 9980;
        `}
                className={CustomStyle.custom_frame}
                id="MapSelection"
            >
                <div style={{ position: "absolute", left: -99999 + "px" }} id="clickMapSelection" onClick={evt => {
                    this.clickMapSelection();
                }}></div>
                <nav className={CustomStyle.custom_nuv} id="MapSelectionDrag">
                    <div className={CustomStyle.box} style={{ marginBottom: 10 + "px" }}>
                        <div className={CustomStyle.item}>
                            <span className={CustomStyle.custom_title}>申請地番</span>
                        </div>
                        <div className={CustomStyle.item}>
                            <button className={CustomStyle.custom_button} onClick={() => {
                                this.checkNextGeneralConditionDiagnosisView();
                            }} id="shindanBtn">概況診断</button>
                        </div>
                    </div>
                    <div className={CustomStyle.box}>
                        <div className={CustomStyle.item}>
                        {!this.props.viewState.showCharacterSelection && (
                            <button className={CustomStyle.custom_button} onClick={() => {
                                this.clearTooltips();
                                this.props.viewState.showCharacterSelectionView();
                            }} id="mojiBtn">文字選択</button>
                        )}
                        {this.props.viewState.showCharacterSelection && (
                            <button className={CustomStyle.custom_button} onClick={() => {
                                this.tooltips();
                                this.props.viewState.hideCharacterSelectionView();
                            }} id="mapBtn">地図選択</button>
                        )}
                        <button style={{ position: "relative", left: 20 + "px", width: 140 + "px",display:"none"}} className={CustomStyle.custom_button} onClick={() => {
                                this.drawingMapSelection();
                            }} id="freeBtn">自由形選択取得</button>
                        </div>
                        <div className={CustomStyle.item}>
                            <button className={CustomStyle.custom_button} onClick={() => {
                                this.back();
                            }} id="backBtn">戻る</button>
                        </div>
                    </div>
                </nav>
                <Box
                    centered
                    paddedHorizontally={5}
                    paddedVertically={10}
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    <table className={CustomStyle.selection_table}>
                        <thead>
                            {Object.keys(table).length > 0 && (
                                <tr className={CustomStyle.table_header}>
                                    {Object.keys(table).map(tableKey => (
                                        <th style={{ width: table[tableKey].tableWidth + "%" }}>{table[tableKey].displayColumnName}</th>
                                    ))}
                                    <th style={{ width: "10%" }}><div style={{ width: "70px" }}></div></th>
                                </tr>
                            )}
                        </thead>
                        <tbody>
                            {Object.keys(applicationPlace).map(idx => (
                                <tr>
                                    {Object.keys(table).map(tableKey => (
                                        <>
                                            {applicationPlace[idx]?.attributes && table[tableKey]?.responseKey &&
                                                <td key={"table" + tableKey} style={{ width: table[tableKey]?.tableWidth + "%" }}>{applicationPlace[idx]?.attributes[table[tableKey]?.responseKey]}</td>
                                            }
                                        </>
                                    ))}
                                    <td style={{ width: "10%" }}><button className={CustomStyle.trash_button} onClick={() => {
                                        this.deleteApplicationPlace(idx);
                                    }}><Icon style={{ fill: "#fff", height: 25 + "px", display: "block", margin: "0 auto" }} glyph={Icon.GLYPHS.trashcan} />
                                    </button></td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </Box>
            </Box >
            </>
        );
    }
}
export default withTranslation()(withTheme(MapSelection));