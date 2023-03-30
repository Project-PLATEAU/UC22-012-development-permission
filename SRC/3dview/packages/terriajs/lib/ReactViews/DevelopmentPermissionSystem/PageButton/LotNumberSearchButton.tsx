import React from "react";
import { useTranslation } from "react-i18next";

import ViewState from "../../../ReactViewModels/ViewState";
import Icon from "../../../Styled/Icon";

import Styles from "../../Map/HelpButton/help-button.scss";


interface Props {
  viewState: ViewState;
}

export default (props: Props) => {
  const { t } = useTranslation();

  return (
    <div>
      <button
        className={Styles.helpBtn}
        onClick={evt => {
          evt.preventDefault();
          evt.stopPropagation();
          if(props.viewState.showApplicationCategorySelection || 
            props.viewState.showMapSelection || 
            props.viewState.showCharacterSelection ||
            props.viewState.showGeneralConditionDiagnosis ||
            props.viewState.showEnterApplicantInformation ||
            props.viewState.showUploadApplicationInformation ||
            props.viewState.showConfirmApplicationDetails)
          {
            alert("申請の処理を終えてから実行してください");
          }else{
            props.viewState.showLotNumberSearchView();
          }
        }}
      >
        <Icon glyph={Icon.GLYPHS.lotSearch} />
        <span>地番検索</span>
      </button>
    </div>
  );
};
