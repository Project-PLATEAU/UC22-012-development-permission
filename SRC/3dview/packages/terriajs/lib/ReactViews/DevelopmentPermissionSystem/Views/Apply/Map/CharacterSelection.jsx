import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Box from "../../../../../Styled/Box";
import CustomStyle from "./scss/character-selection.scss";
import CommonStrata from "../../../../../Models/Definition/CommonStrata";
import webMapServiceCatalogItem from '../../../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import LotNumberSearch from "../../LotNumberSearch/LotNumberSearch.jsx";
import Config from "../../../../../../customconfig.json";
/**
 * 文字選択画面
 */
@observer
class CharacterSelection extends React.Component {
    static displayName = "CharacterSelection";
    static propTypes = {
        terria: PropTypes.object.isRequired,
        viewState: PropTypes.object.isRequired,
        theme: PropTypes.object,
        t: PropTypes.func.isRequired
    };
    constructor(props) {
        super(props);
        this.state = {
            viewState: props.viewState,
            terria: props.terria
        };
    }

    //選択済み申請地の変更処理
    changeApplicationPlace(){
        this.props.viewState.changeApplicationPlace();
        try{
            const item = new webMapServiceCatalogItem(Config.layer.lotnumberSearchLayerNameForSelected, this.state.terria);
            const wmsUrl = Config.config.geoserverUrl;
            const items = this.state.terria.workbench.items;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForSelected) {
                    this.state.terria.workbench.remove(aItem);
                    aItem.loadMapItems();
                }
            }
            if(Object.keys(this.props.viewState.applicationPlace).length > 0){
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
        }catch(e){
            console.error('処理に失敗しました', error);
        }
    }

    render() {
        return (
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                styledWidth={"604px"}
                styledHeight={"500px"}
                fullHeight
                id="CharacterSelection"
                css={`
          position: fixed;
          z-index: 9980;
        `}
                className={CustomStyle.custom_frame}
            >
                <div className={CustomStyle.box}>
                    <div className={CustomStyle.item}>
                        <LotNumberSearch terria={this.state.terria} viewState={this.props.viewState} />
                    </div>
                    <div className={CustomStyle.item}>
                        <button className={CustomStyle.circle_button} onClick={e => this.changeApplicationPlace()}>&gt;</button>
                    </div>
                </div>

            </Box >
        );
    }
}
export default withTranslation()(withTheme(CharacterSelection));