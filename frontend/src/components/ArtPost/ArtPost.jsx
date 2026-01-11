import { useState } from 'react';
import styles from './ArtPost.module.css';
import LikeBtn from '../LikeBtn/LikeBtn';
import DefaultBtn from '../DefaultBtn/DefaultBtn';
import CustomTextArea from '../CustomTextArea/CustomTextArea';

import editIcon from '../../assets/edit-profile-icon.svg'

export default function ArtPost({ edited = false, isOwner = false, image }) {
    const MAX_LENGTH = 500;
    const [description, setDescription] = useState('');
    const [tags, setTags] = useState('');

    const handleTagsChange = (e) => {
        if (e.target.value.length <= MAX_LENGTH) {
            setTags(e.target.value);
        }
    };

    const handleDescriptionChange = (e) => {
        if (e.target.value.length <= MAX_LENGTH) {
            setDescription(e.target.value);
        }
    };

    return(
        <div className={styles.artWrapp}>
            <div className={styles.artImage}>
                <img 
                    src={image} 
                    alt="art" 
                    className={styles.art}
                />
            </div>

            {!edited &&(
                <div className={styles.content}>
                    <div className={styles.textContent}>
                            <div className={styles.tags}>
                                <span>#kfldfk #dkslk #dfjsldf #dkfldskf</span>
                            </div>
                            <span>mamds podvvv;d|dspcdosk fdfldskf;dlfsd ;fkds;fdkf;fdlkfdfkdl fkld lf dkfldfk ldl kdfifk</span>
                        </div>
                        <LikeBtn className={styles.like}/>
                </div>
            )}
            
            {edited && (
                <div className={styles.editContent}>
                    <div className={styles.form}>
                        <CustomTextArea
                            value={tags}
                            onChange={handleTagsChange}
                            maxLength={MAX_LENGTH}
                            placeholder="например: #rec #fyp #wenclair.."
                            label="Тэги:"
                            id="tags"
                        />

                        <CustomTextArea
                            value={description}
                            onChange={handleDescriptionChange}
                            maxLength={MAX_LENGTH}
                            placeholder="Пара слов.."
                            label="Описание:"
                            id="description"
                        />
                    </div>
                    <div className={styles.btnArea}>
                        <DefaultBtn text={'Сохранить'} />
                    </div>
                </div>
            )}

            {!edited && isOwner && (
                <div className={styles.edit}>
                    <img src={editIcon} alt="arts" />
                    <span>Редактировать</span>
                </div>
            )}

        </div>
    );
}