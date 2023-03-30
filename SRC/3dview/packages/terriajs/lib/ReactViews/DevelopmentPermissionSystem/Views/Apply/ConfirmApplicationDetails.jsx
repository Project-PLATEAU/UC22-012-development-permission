import { observer } from "mobx-react";
import PropTypes from "prop-types";
import React from "react";
import { withTranslation } from "react-i18next";
import { withTheme } from "styled-components";
import Icon, { StyledIcon } from "../../../../Styled/Icon";
import Spacing from "../../../../Styled/Spacing";
import Box from "../../../../Styled/Box";
import Button, { RawButton } from "../../../../Styled/Button";
import CustomStyle from "./scss/confirm-application-details.scss";
import CustomGeneralStyle from "./scss/general-condition-diagnosis.scss";
import Config from "../../../../../customconfig.json";
import GeneralConditionDiagnosis from "./GeneralConditionDiagnosis.jsx";
/**
 * 申請内容確認画面
 */
@observer
class ConfirmApplicationDetails extends React.Component {
    static displayName = "ConfirmApplicationDetails";
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
            terria: props.terria
        };
    }
    
    componentDidMount() {
        document.getElementById("customloader").style.display = "none";
        this.draggable(document.getElementById('applicationFrameDrag'),document.getElementById('applicationFrame'));
    }

    //登録処理
    register(){
        document.getElementById("applicationFrame").scrollTop = 0;
        document.getElementById("customloader").style.display = "block";
        let generalConditionDiagnosisResult = Object.values(this.props.viewState.generalConditionDiagnosisResult);
        generalConditionDiagnosisResult = generalConditionDiagnosisResult.filter(Boolean);
        let checkedApplicationCategory = Object.values(this.props.viewState.checkedApplicationCategory);
        checkedApplicationCategory = checkedApplicationCategory.filter(Boolean);
        let applicationPlace = Object.values(this.props.viewState.applicationPlace);
        applicationPlace = applicationPlace.filter(Boolean);
        let applicantInformation = Object.values(this.props.viewState.applicantInformation);
        applicantInformation = applicantInformation.filter(Boolean);
        let notifyCount = 0;
        fetch(Config.config.apiUrl + "/application/register", {
            method: 'POST',
            body: JSON.stringify({
                generalConditionDiagnosisResultForm:generalConditionDiagnosisResult,
                applicationCategories:checkedApplicationCategory,
                lotNumbers: applicationPlace,
                applicantInformationItemForm:applicantInformation,
                folderName:this.props.viewState.folderName

            }),
            headers: new Headers({ 'Content-type': 'application/json' }),
        })
        .then(res => res.json())
        .then(res => {
            if (res.applicationId) {
                let applicationFile = this.props.viewState.applicationFile;
                let resApplicationId = res.applicationId;
                let fileCount = 0;
                if(Object.keys(applicationFile).length > 0){
                    Object.keys(applicationFile).map(key => {
                        Object.keys(applicationFile[key]["uploadFileFormList"]).map(fileKey => {
                            applicationFile[key]["uploadFileFormList"][fileKey]["applicationId"] = res.applicationId;
                            applicationFile[key]["uploadFileFormList"][fileKey]["applicationFileId"] = applicationFile[key]["applicationFileId"];
                            const formData  = new FormData();
                            for(const name in applicationFile[key]["uploadFileFormList"][fileKey]) {
                                formData.append(name, applicationFile[key]["uploadFileFormList"][fileKey][name]);
                            }
                            fileCount = fileCount + 1;
                            fetch(Config.config.apiUrl + "/application/file/upload", {
                                method: 'POST',
                                body: formData,
                            })
                            .then(res => res.json())
                            .then(res => {
                                applicationFile[key]["uploadFileFormList"][fileKey]["status"] = res.status;
                                if(res.status !== 201){
                                    alert('アップロードに失敗しました');
                                }
                                let completeFlg = true;
                                Object.keys(applicationFile).map(key => { 
                                    Object.keys(applicationFile[key]["uploadFileFormList"]).map(fileKey => {
                                        if(!applicationFile[key]["uploadFileFormList"][fileKey]["status"]){
                                            completeFlg = false;
                                        }
                                    })
                                })
                                if(completeFlg){
                                    notifyCount = notifyCount + 1;
                                    this.notify(resApplicationId,notifyCount);               
                                }
                            }).catch(error => {
                                document.getElementById("customloader").style.display = "none";
                                console.error('処理に失敗しました', error);
                                alert('処理に失敗しました');
                            });
                        });
                    });
                    if(fileCount === 0){
                        notifyCount = notifyCount + 1;
                        this.notify(resApplicationId,notifyCount);  
                    }
                }else{
                    notifyCount = notifyCount + 1;
                    this.notify(resApplicationId,notifyCount); 
                }
            }else{
                document.getElementById("customloader").style.display = "none";
                alert("申請の登録処理に失敗しました");
            }
        }).catch(error => {
            document.getElementById("customloader").style.display = "none";
            console.error('処理に失敗しました', error);
            alert('処理に失敗しました');
        });
    }

    /**
     * 申請登録完了通知処理
     * @param {number} resApplicationId 申請ID
     * @param {number} notifyCount 呼び出し回数
     */
    notify(resApplicationId,notifyCount){
        if(resApplicationId && notifyCount ===1){
            fetch(Config.config.apiUrl + "/application/notify/collation", {
                method: 'POST',
                body: JSON.stringify({
                    applicationId:resApplicationId,
                }),
                headers: new Headers({ 'Content-type': 'application/json' }),
            })
            .then(res => res.json())
            .then(res => {
                if (res.loginId) {
                    const loginId = res.loginId;
                    const password = res.password;
                    fetch(Config.config.apiUrl + "/label/1001")
                    .then(res => res.json())
                    .then(res => {
                        if (Object.keys(res).length > 0) {
                            this.props.viewState.setCustomMessage(res[0]?.labels?.title,res[0]?.labels?.content)
                            document.getElementById("customloader").style.display = "none";
                            this.props.viewState.nextApplicationCompletedView(loginId,password);
                        }else{
                            document.getElementById("customloader").style.display = "none";
                            alert("labelの取得に失敗しました。");
                        }
                    }).catch(error => {
                        document.getElementById("customloader").style.display = "none";
                        console.error('通信処理に失敗しました', error);
                        alert('通信処理に失敗しました');
                    });
                }else{
                    document.getElementById("customloader").style.display = "none";
                    alert('照合情報通知処理に失敗しました');
                }
            }).catch(error => {
                document.getElementById("customloader").style.display = "none";
                console.error('通信処理に失敗しました', error);
                alert('通信処理に失敗しました');
            });
        }
    }

    // 申請登録前帳票出力処理
    generalConditionOutput(){
        if(document.getElementById("generalConditionOutputBtn")){
            document.getElementById("generalConditionOutputBtn").click();
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

    // 申請対象レイヤをリセット
    clearLayer() {
        try{
            const items = this.state.terria.workbench.items;
            for (const aItem of items) {
                if (aItem.uniqueId === Config.layer.lotnumberSearchLayerNameForApplicationTarget) {
                    this.state.terria.workbench.remove(aItem);
                    aItem.loadMapItems();
                }
            }
        }catch(e){
            console.error('処理に失敗しました', error);
        }
    }

    render() {
        const explanation = "以下の内容で申請を行います。よろしいでしょうか？";
        const checkedApplicationCategory = this.props.viewState.checkedApplicationCategory;
        const applicantInformation = this.props.viewState.applicantInformation;
        const applicationFile = this.props.viewState.applicationFile;
        const applicationPlace = this.props.viewState.applicationPlace;
        let applicationPlaceResult = {};
        Object.keys(applicationPlace).map(key => {
            if(!applicationPlaceResult[applicationPlace[key].districtName]){
                applicationPlaceResult[applicationPlace[key].districtName] = new Array();
            }
            applicationPlaceResult[applicationPlace[key].districtName].push(applicationPlace[key].chiban);
        });

        return (
            <>
            <div className={CustomGeneralStyle.loadingBg} id="loadingBg"></div>
            <div className={CustomGeneralStyle.loading} id="loading">
                <p style={{ textAlign: "center" }}>申請処理中です。画面はこのままでお待ちください。</p>
                <p style={{ textAlign: "center" }}>※バックグラウンド動作の場合正常にキャプチャの切替が行われませんのでご注意ください。</p>
                <p style={{ textAlign: "center" }}>画面キャプチャ取得中 <span id="numberOfSheets">0/0</span></p>
                <div className={CustomGeneralStyle.myProgress}>
                    <div className={CustomGeneralStyle.myBar} id="myBar"></div>
                </div>
                <p style={{ textAlign: "center" }}>申請帳票作成中 <span id="wholePercent">0</span>%</p>
            </div>
            <div style={{ display: "none" }}>
                <GeneralConditionDiagnosis terria={this.props.terria} viewState={this.props.viewState} />
            </div>
            <div style={{ display: "none" }} id="confirmApplicationDetailsRegisterButton" onClick={e => {this.register();}}></div>
            <Box
                displayInlineBlock
                backgroundColor={this.props.theme.textLight}
                styledWidth={"600px"}
                styledHeight={"600px"}
                fullHeight
                id="applicationFrame"
                css={`
          position: fixed;
          z-index: 9992;
        `}
                className={CustomStyle.custom_frame}
            >
                <div id="customloader" className={CustomStyle.customloaderParent}>
                    <div className={CustomStyle.customloader}>Loading...</div>
                </div>
                <Box position="absolute" paddedRatio={3} topRight>
                    <RawButton onClick={() => {
                        this.clearLayer();
                        this.props.viewState.hideConfirmApplicationDetailsView();
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
                <nav className={CustomStyle.custom_nuv} id="applicationFrameDrag">
                    申請確認
                </nav>
                <Box
                    centered
                    paddedHorizontally={5}
                    paddedVertically={10}
                    displayInlineBlock
                    className={CustomStyle.custom_content}
                >
                    <p className={CustomStyle.explanation}>{explanation}</p>
                    <Spacing bottom={1} />
                    <div className={CustomStyle.scrollContainer}>
                    <p>■申請者情報</p>
                    {Object.keys(applicantInformation).map(key => (
                        <div className={CustomStyle.box}>
                            <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                ・{applicantInformation[key].name}
                            </div>
                            <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                {applicantInformation[key].value}
                            </div>
                        </div>
                    ))}
                    {Object.keys(checkedApplicationCategory).map(key => (
                        <div className={CustomStyle.box}>
                            <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                                ■{checkedApplicationCategory[key]["title"]}
                            </div>
                            <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                                {checkedApplicationCategory[key]["applicationCategory"]?.map(function (value) { return value.content }).filter(content => {return content !== null}).join(",")}
                            </div>
                        </div>
                    ))}
                    <div className={CustomStyle.box}>
                        <div className={CustomStyle.item} style={{ width: 60 + "%" }}>
                            ■申請地番
                        </div>
                        <div className={CustomStyle.item} style={{ width: 40 + "%" }}>
                            {Object.keys(applicationPlaceResult).map(key => (
                                <p>{key} {applicationPlaceResult[key].map(chiban => { return chiban }).filter(chiban => {return chiban !== null}).join(",")}</p>
                            ))}
                        </div>
                    </div>
                    <Spacing bottom={3} />
                    <table className={CustomStyle.selection_table}>
                        <thead>
                            <tr className={CustomStyle.table_header}>
                                <th>対象</th>
                                <th style={{ width: 50 + "px"}}>拡張子</th>
                                <th style={{ width: 280 + "px"}}>ファイル名</th>
                            </tr>
                        </thead>
                        <tbody>
                            {Object.keys(applicationFile).map(key => (
                                applicationFile[key].applicationFileId !== "9999" && (
                                <tr>
                                    <td>
                                        {applicationFile[key].applicationFileName}
                                    </td>
                                    <td>pdf</td>
                                    <td>{applicationFile[key] && (
                                        applicationFile[key]["uploadFileFormList"].map(uploadApplicationFile => { return uploadApplicationFile.uploadFileName }).filter(uploadFileName => {return uploadFileName !== null}).join(",")
                                    )}</td>
                                </tr>
                                )
                            ))}
                        </tbody>
                    </table>
                    </div>
                    <Spacing bottom={3} />
                    <div className={CustomStyle.box}>
                        <div className={CustomStyle.item} style={{ width: 48 + "%" }}>
                            <button
                                className={CustomStyle.next_button}
                                onClick={e => {
                                    this.generalConditionOutput();
                                }}
                            >
                                <span>申請</span>
                            </button>
                        </div>
                        <div className={CustomStyle.item} style={{ width: 48 + "%" }}>
                            <button
                                className={CustomStyle.back_button}
                                onClick={e => {
                                    this.clearLayer();
                                    this.props.viewState.backUploadApplicationInformationView();
                                }}
                            >
                                <span>戻る</span>
                            </button>
                        </div>
                    </div>
                </Box>
            </Box >
            </>
        );
    }
}
export default withTranslation()(withTheme(ConfirmApplicationDetails));