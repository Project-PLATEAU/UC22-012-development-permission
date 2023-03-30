import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import Button, { RawButton } from "../../../../Styled/Button";
import Select from "../../../../Styled/Select";
import CustomStyle from "./scss/application-category-selection.scss";
import Config from "../../../../../customconfig.json";
/**
 * 申請区分選択画面
 */
@observer
class ApplicationCategorySelection extends React.Component {
    static displayName = "ApplicationCategorySelection";
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
            //画面
            screen: [{}],
            //画面index
            index: 0,
            //申請区分
            applicationCategory: [[{}]],
            //選択済み申請区分
            checkedApplicationCategory: [[{}]]

        };
    }
    componentDidMount() {
        let screen = this.state.screen;
        let applicationCategory = this.state.applicationCategory;
        let checkedApplicationCategory = this.state.checkedApplicationCategory;
        let index = this.state.index;
        // APIへのリクエスト・レスポンス結果取得処理
        fetch(Config.config.apiUrl + "/category/views/")
        .then(res => res.json())
        .then(res => {
            if (Object.keys(res).length > 0  && !res.status) {
                screen = res;
            }else{
                alert("区分選択画面の項目一覧の取得に失敗しました。再度操作をやり直してください。");
                this.props.viewState.hideApplicationCategorySelectionView(); 
            }
            Object.keys(screen).map(key => {
                applicationCategory[key] = screen[key].applicationCategory;
                // チェック状態初期化
                for (let i = 0; i < applicationCategory[key].length; i++) {
                    applicationCategory[key][i].checked = false;
                }
            });
            //　画面に対応する選択状態の申請区分がある場合、再度選択済みとして状態を更新
            //　選択状態が無い場合状態の初期化を行う
            Object.keys(screen).map(key => {
                if (this.props.viewState.checkedApplicationCategoryLocalSave[key]) {
                    checkedApplicationCategory[key] = this.props.viewState.checkedApplicationCategoryLocalSave[key];
                    // select boxの場合初期化
                    Object.keys(checkedApplicationCategory[key]).map(categoryKey => {
                        if (!screen[key].multiple && checkedApplicationCategory[key][categoryKey].checked) {
                            let index = applicationCategory[key].findIndex((v) => v.id === checkedApplicationCategory[key][categoryKey].id);
                            if(index >= 0){
                                applicationCategory[key][index].checked = true;
                            }
                        }
                    });
                    // screenのindex情報を更新
                    index = key;
                } else {
                    checkedApplicationCategory[key] = [{}];
                    // selectboxの場合は先頭値を選択済みに追加しておく
                    if (!screen[key].multiple && applicationCategory[key][0]) {
                        applicationCategory[key][0].checked = true;
                        checkedApplicationCategory[key][0] = Object.assign({}, applicationCategory[key][0]);
                    }
                }
            });
            this.setState({ screen: screen, applicationCategory: applicationCategory, checkedApplicationCategory: checkedApplicationCategory,index:index })
        }).catch(error => {
            console.error('通信処理に失敗しました', error);
            alert('通信処理に失敗しました');
        });
        this.draggable(document.getElementById('ApplicationCategorySelectionDrag'),document.getElementById('ApplicationCategorySelection'));
        
    }
    // 選択済みに追加する
    addCheckedApplicationCategory() {
        const index = this.state.index;
        let applicationCategory = this.state.applicationCategory;
        let checkedApplicationCategory = this.state.checkedApplicationCategory;
        Object.keys(applicationCategory[index]).map(key => {
            if (applicationCategory[index][key].checked) {
                checkedApplicationCategory[index][key] = Object.assign({}, applicationCategory[index][key]);
                checkedApplicationCategory[index][key].checked = false;
            }
            applicationCategory[index][key].checked = false;
        });
        this.setState({ checkedApplicationCategory: checkedApplicationCategory, applicationCategory: applicationCategory });
    }
    // 選択済みから削除する
    deleteCheckedApplicationCategory() {
        const index = this.state.index;
        let checkedApplicationCategory = this.state.checkedApplicationCategory;
        Object.keys(checkedApplicationCategory[index]).map(key => {
            if (checkedApplicationCategory[index][key].checked) {
                delete checkedApplicationCategory[index][key];
            }
        });
        this.setState({ checkedApplicationCategory: checkedApplicationCategory });
    }
    // 項目一覧のcheckbox処理
    handleCheckBoxAdd(event) {
        const index = this.state.index;
        let applicationCategory = this.state.applicationCategory;
        applicationCategory[index][event.target.value].checked = event.target.checked;
        this.setState({ applicationCategory: applicationCategory });
    }
    // 選択済みのcheckbox処理
    handleCheckBoxDelete(event) {
        const index = this.state.index;
        let checkedApplicationCategory = this.state.checkedApplicationCategory;
        checkedApplicationCategory[index][event.target.value].checked = event.target.checked;
        this.setState({ checkedApplicationCategory: checkedApplicationCategory });
    }
    // selectbox処理
    handleSelectBoxAdd(event) {
        const index = this.state.index;
        let applicationCategory = this.state.applicationCategory;
        let checkedApplicationCategory = this.state.checkedApplicationCategory;
        //　一旦全てのcheckedをfalseに設定
        Object.keys(applicationCategory[index]).map(key => {
            if (applicationCategory[index][key].checked) {
                applicationCategory[index][key].checked = false;
            }
            if (checkedApplicationCategory[index][key]) {
                delete checkedApplicationCategory[index][key];
            }
        });
        applicationCategory[index][event.target.value].checked = true;
        checkedApplicationCategory[index][event.target.value] = Object.assign({}, applicationCategory[index][event.target.value]);
        this.setState({ applicationCategory: applicationCategory, checkedApplicationCategory: checkedApplicationCategory });
    }
    // 次へ
    next() {
        const screen = this.state.screen;
        const checkedApplicationCategory = this.state.checkedApplicationCategory;
        let permission = false;
        let index = this.state.index;
        // 項目選択が必須の画面の場合は選択済み状態のcheck
        if (screen[index].require) {
            Object.keys(checkedApplicationCategory[index]).map(key => {
                if (checkedApplicationCategory[index][key]?.id) {
                    permission = true;
                }
            });
        } else {
            permission = true;
        }
        // 検証用check処理
        if (!permission) {
            alert("何れかの項目を選択してください");
        } else {
            if (permission && this.state.screen.length > index + 1) {
                this.setState({ index: index + 1 });
            } else {
                let checkedApplicationCategoryCopy = Object.assign({}, checkedApplicationCategory);
                Object.keys(checkedApplicationCategoryCopy).map(key => {
                    checkedApplicationCategoryCopy[key] = checkedApplicationCategoryCopy[key].filter(function (s) { return Object.keys(s).length > 0 });
                });
                let checkedApplicationCategoryResult = {};
                Object.keys(screen).map(key => {
                    checkedApplicationCategoryResult[key] = JSON.parse(JSON.stringify(screen[key]));
                    checkedApplicationCategoryResult[key]["applicationCategory"] = checkedApplicationCategoryCopy[key];
                });
                // 選択済みの項目を保持して画面遷移
                this.props.viewState.nextCharacterSelectionView(checkedApplicationCategoryResult,checkedApplicationCategory);
            }
        }
    }
    // 戻る
    back() {
        let index = this.state.index;
        if (0 <= index - 1) {
            this.setState({ index: index - 1 });
        } else {
            this.props.viewState.hideApplicationCategorySelectionView();
        }
    }

    /**
     * コンポーネントドラッグ操作
     * @param {Object} ドラッグ操作対象
     * @param {Object} ドラッグ対象
     */
    draggable(target,content) {
        target.onmousedown = function() {
            document.onmousemove = mouseMove;
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
        const index = this.state.index;
        const title = (parseInt(index) + 1) + ". " + this.state.screen[index].title;
        const screen = this.state.screen;
        const explanation = this.state.screen[index].explanation;
        const applicationCategory = this.state.applicationCategory;
        // 選択済み申請区分、画面表示用に詰め替え
        let checkedApplicationCategoryCopy = Object.assign({}, this.state.checkedApplicationCategory);
        Object.keys(checkedApplicationCategoryCopy).map(key => {
            checkedApplicationCategoryCopy[key] = checkedApplicationCategoryCopy[key].filter(function (s) { return Object.keys(s).length > 0 });
        });
        return (
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                styledWidth={"520px"}
                styledHeight={"600px"}
                fullHeight
                css={`
          position: fixed;
          z-index: 9991;
        `}
                id="ApplicationCategorySelection"
                className={CustomStyle.custom_frame}
            >
                <Box position="absolute" paddedRatio={3} topRight>
                    <RawButton onClick={() => {
                        this.props.viewState.hideApplicationCategorySelectionView();
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
                <nav className={CustomStyle.custom_nuv} id="ApplicationCategorySelectionDrag">
                    申請区分選択
                </nav>
                {screen[index].screenId && (
                <Box
                    centered
                    paddedHorizontally={5}
                    paddedVertically={10}
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    <h2 className={CustomStyle.title}>{title}</h2>
                    <div className={CustomStyle.scrollContainer}>
                    {screen[index].multiple && (
                        <>
                        <p className={CustomStyle.table_explanation}><span dangerouslySetInnerHTML={{ __html: explanation }}></span></p>
                        <div className={CustomStyle.box}>
                            <div className={CustomStyle.item}>
                                <h3 className={CustomStyle.sub_title}>項目一覧</h3>
                                <table className={CustomStyle.selection_table}>
                                    <tbody>
                                        {Object.keys(applicationCategory[index]).map(key => (
                                            <tr key={key}>
                                                <td className={CustomStyle.checkbox}>
                                                    {applicationCategory[index][key]?.id && (
                                                        <input type="checkbox" value={key} onChange={e => this.handleCheckBoxAdd(e)} checked={applicationCategory[index][key]?.checked} />
                                                    )}
                                                </td>
                                                <td className={CustomStyle.content}>
                                                    {applicationCategory[index][key]?.id && (
                                                        applicationCategory[index][key]?.content
                                                    )}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                            <div className={CustomStyle.item}>
                                <button className={CustomStyle.circle_button} style={{ position: "relative", top: 15 + "%", cursor: "pointer", }} onClick={e => this.addCheckedApplicationCategory()}>&gt;</button>
                                <button className={CustomStyle.circle_button} style={{ position: "relative", top: 30 + "%", cursor: "pointer", }} onClick={e => this.deleteCheckedApplicationCategory()}>&lt;</button>
                            </div>
                            <div className={CustomStyle.item}>
                                <h3 className={CustomStyle.sub_title}>選択済み</h3>
                                <table className={CustomStyle.selection_table}>
                                    <tbody>
                                        {Object.keys(applicationCategory[index]).map(key => (
                                            <tr key={key}>
                                                <td className={CustomStyle.checkbox}>
                                                    {checkedApplicationCategoryCopy[index][key]?.id && (
                                                        <input type="checkbox" value={applicationCategory[index].findIndex((v) => v.id === checkedApplicationCategoryCopy[index][key]?.id)} onChange={e => this.handleCheckBoxDelete(e)} checked={checkedApplicationCategoryCopy[index][key]?.checked} />
                                                    )}
                                                </td>
                                                <td className={CustomStyle.content}>
                                                   {checkedApplicationCategoryCopy[index][key]?.id && (
                                                        checkedApplicationCategoryCopy[index][key]?.content
                                                    )}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        </>
                    )}
                    {!screen[index].multiple && (
                        <>
                        <p className={CustomStyle.explanation}><span dangerouslySetInnerHTML={{ __html: explanation }}></span></p>
                        <div className={CustomStyle.box}>
                            <div className={CustomStyle.select_box}>
                                <Select
                                    light={true}
                                    dark={false}
                                    onChange={e => this.handleSelectBoxAdd(e)}
                                    style={{ color: "#000" }}>
                                    {Object.keys(applicationCategory[index]).map(key => (
                                        <option key={applicationCategory[index][key]?.content} value={key} selected={applicationCategory[index][key]?.checked}>
                                            {applicationCategory[index][key]?.content}
                                        </option>
                                    ))}
                                </Select>
                            </div>
                        </div>
                        </>
                    )}
                    </div>
                    <Spacing bottom={3} />
                    <div className={CustomStyle.box}>
                        <div className={CustomStyle.item} style={{ width: 48 + "%" }}>
                            <button
                                className={CustomStyle.next_button}
                                onClick={e => {
                                    this.next();
                                }}
                            >
                                <span>次へ</span>
                            </button>
                        </div>
                        <div className={CustomStyle.item} style={{ width: 48 + "%" }}>
                            <button
                                className={CustomStyle.back_button}
                                onClick={e => {
                                    this.back();
                                }}
                            >
                                <span>戻る</span>
                            </button>
                        </div>
                    </div>
                </Box >
                )}
            </Box >
        );
    }
}
export default withTranslation()(withTheme(ApplicationCategorySelection));