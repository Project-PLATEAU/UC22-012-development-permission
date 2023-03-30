import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../Styled/Icon";
import Spacing from "../../Styled/Spacing";
import Text from "../../Styled/Text";
import Checkbox from "../../Styled/Checkbox";
import Input from "../../Styled/Input";
import Box from "../../Styled/Box";
import Button, { RawButton } from "../../Styled/Button";
import Select from "../../Styled/Select";
import CustomStyle from "./scss/application-completed.scss";
import CommonStrata from "../../Models/Definition/CommonStrata";
import webMapServiceCatalogItem from '../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import { useState } from 'react';
@observer
class CustomMessage extends React.Component {
    static displayName = "CustomMessage";
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

    render() {
        return (
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                styledWidth={"600px"}
                styledHeight={"200px"}
                fullHeight
                overflow={"hidden"}
                css={`
          position: fixed;
          z-index: 99999;
        `}
                className={CustomStyle.custom_frame}
            >
                <Box position="absolute" paddedRatio={3} topRight>
                    <RawButton onClick={() => {
                        this.props.viewState.hideCustomMessageView();
                    }}>
                        <StyledIcon
                            styledWidth={"16px"}
                            fillColor={this.props.theme.textLight}
                            opacity={"0.5"}
                            glyph={Icon.GLYPHS.closeLight}
                            css={`
                            cursor:pointer;
                            fill:#000000;
                          `}
                        />
                    </RawButton>
                </Box>
                <Box
                    centered
                    paddedHorizontally={5}
                    paddedVertically={10}
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    <p className={CustomStyle.center}>
                        <span dangerouslySetInnerHTML={{ __html: this.props.viewState.customMessageTitle }}></span>
                    </p>
                    <p className={CustomStyle.center}>
                        <span dangerouslySetInnerHTML={{ __html: this.props.viewState.customMessageContent }}></span>
                    </p>
                   
                </Box>
            </Box >
        );
    }
}
export default withTranslation()(withTheme(CustomMessage));