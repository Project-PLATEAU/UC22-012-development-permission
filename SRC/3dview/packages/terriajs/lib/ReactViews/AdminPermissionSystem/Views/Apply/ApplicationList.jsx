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
import CustomStyle from "./scss/application-information-search.scss";
import CommonStrata from "../../../../Models/Definition/CommonStrata";
import webMapServiceCatalogItem from '../../../../Models/Catalog/Ows/WebMapServiceCatalogItem';
import { useState } from 'react';
import Common from "../../../AdminDevelopCommon/CommonFixedValue.json";
import Config from "../../../../../customconfig.json";

@observer
class ApplicationList extends React.Component {
    static displayName = "ApplicationList";
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
            applicationList: props.viewState.applicationList
        };
    }

    componentDidMount() {
        this.draggable(document.getElementById('ApplicationListDrag'), document.getElementById('ApplicationList'));
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
        const applicationList = this.state.applicationList;
        return (
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                styledWidth={"310px"}
                styledHeight={"300px"}
                fullHeight
                id="ApplicationList"
                overflow={"auto"}
                css={`
          position: fixed;
          z-index: 9991;
        `}
                className={CustomStyle.custom_frame}
            >
                <Box position="absolute" paddedRatio={3} topRight>
                    <RawButton onClick={() => {
                        this.props.viewState.hideApplicationListView();
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
                <nav className={CustomStyle.custom_nuv} id="ApplicationListDrag">
                    申請一覧
                </nav>
                <Box
                    centered
                    paddedHorizontally={5}
                    paddedVertically={10}
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                    style={{ width: "95%" }}
                >
                    <p className={CustomStyle.explanation}>対象地番には複数の申請があります<br/>{applicationList[0].districtName+" "+applicationList[0].chiban}</p>
                    <Spacing bottom={1} />
                    <div className={CustomStyle.scrollContainer} style={{ height: "150px" }}>
                        <table className={CustomStyle.selection_table}>
                            <thead>
                                <tr className={CustomStyle.table_header}>
                                    <th style={{ width: "60px" }}>申請ID</th>
                                    <th>申請</th>
                                    <th style={{ width: "60px" }}></th>
                                </tr>
                            </thead>
                            <tbody>
                                {Object.keys(applicationList).map(key => (
                                    <tr>
                                        <td>
                                            {applicationList[key]?.applicationId}
                                        </td>
                                        <td>
                                            {Common.status[applicationList[key]?.status]}
                                        </td>
                                        <td>
                                            <button
                                                className={CustomStyle.detail_button}
                                                onClick={evt => {
                                                    evt.preventDefault();
                                                    evt.stopPropagation();
                                                    this.props.viewState.nextApplicationDetailsView(applicationList[key]?.applicationId);
                                                }}
                                            >
                                                <span>詳細</span>
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </Box>
            </Box >
        );
    }
}
export default withTranslation()(withTheme(ApplicationList));