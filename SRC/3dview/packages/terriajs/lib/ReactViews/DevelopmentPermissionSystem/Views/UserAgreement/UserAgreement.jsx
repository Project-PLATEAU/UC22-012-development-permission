import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import Text from "../../../../Styled/Text";
import Checkbox from "../../../../Styled/Checkbox";
import Input from "../../../../Styled/Input";
import Box from "../../../../Styled/Box";
import Button, { RawButton } from "../../../../Styled/Button";
import Select from "../../../../Styled/Select";
import CustomStyle from "./scss/user-agreement.scss";
import CommonStrata from "../../../../Models/Definition/CommonStrata";
import webMapServiceCatalogItem from '../../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import { useState } from 'react';
import Config from "../../../../../customconfig.json";
/**
 * 利用者規約画面
 */
@observer
class UserAgreement extends React.Component {
    static displayName = "UserAgreement";
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
            terria: props.terria,
            userAgreement: {}
        };
    }
    componentDidMount() {
        const { t } = this.props;
        let userAgreement = this.state.userAgreement;
        //サーバからlabelを取得
        fetch(Config.config.apiUrl + "/label/1000")
        .then(res => res.json())
        .then(res => {
            if (Object.keys(res).length > 0) {
                userAgreement = { title:  res[0]?.labels?.title, content:  res[0]?.labels?.content, consentButtonText:  res[0]?.labels?.contentButtonText };
                this.setState({ userAgreement: userAgreement });
            }else{
                alert("labelの取得に失敗しました。");
            }
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
    }

    render() {
        const { t } = this.props;
        const title = this.state.userAgreement.title;
        const content = this.state.userAgreement.content;
        const consentButtonText = this.state.userAgreement.consentButtonText;
        return (
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                styledWidth={"600px"}
                styledHeight={"450px"}
                fullHeight
                overflow={"auto"}
                css={`
          position: fixed;
          z-index: 999999;
        `}
                className={CustomStyle.custom_frame}
            >
                <nav className={CustomStyle.custom_nuv}>
                    <span dangerouslySetInnerHTML={{ __html: title }}></span>
                </nav>
                <Box
                    centered
                    paddedHorizontally={5}
                    paddedVertically={10}
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >

                    <Spacing bottom={3} />
                    <div dangerouslySetInnerHTML={{ __html: content }}></div>
                    <Spacing bottom={3} />
                    <div className={CustomStyle.box}>
                        <div className={CustomStyle.item} style={{ width: 48 + "%" }}>
                            <button
                                className={CustomStyle.next_button}
                                onClick={e => {
                                    this.props.viewState.hideUserAgreementView();
                                }}
                            >
                                <span dangerouslySetInnerHTML={{ __html: consentButtonText }}></span>
                            </button>
                        </div>
                        <div className={CustomStyle.item} style={{ width: 48 + "%" }}>
                            <button
                                className={CustomStyle.back_button}
                                onClick={e => {
                                    if( window.history.length >= 2 ) {
                                        window.history.back();
                                        return false;
                                    }
                                    else {
                                        window.close();
                                    }
                                }}
                            >
                                <span>トップに戻る</span>
                            </button>
                        </div>
                    </div>
                </Box>
            </Box >
        );
    }
}
export default withTranslation()(withTheme(UserAgreement));