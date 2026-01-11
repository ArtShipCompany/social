import { Link } from 'react-router-dom';
import { useState } from 'react';
import styles from './ArtPost.module.css';
import LikeBtn from '../LikeBtn/LikeBtn';
import DefaultBtn from '../DefaultBtn/DefaultBtn';
import CustomTextArea from '../CustomTextArea/CustomTextArea';
import PFP from '../../assets/WA.jpg'

import editIcon from '../../assets/edit-profile-icon.svg'

export default function ArtPost({ 
  edited = false, 
  isOwner = false,
  artId,  
  image, 
  description = '', 
  tags = '' 
}) {
    const MAX_LENGTH = 500;
    const [editDescription, setDescription] = useState('');
    const [editTags, setTags] = useState('');

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

            {!edited && (
                <div className={styles.content}>
                    {!isOwner && (
                        <div className={styles.textAndLike}>
                            <div className={styles.owner}>
                                <img src={PFP} alt="" className={styles.pfp}/>
                                <span className={styles.nickname}>@
                                    <span className={styles.link}>crow_666_wa</span>
                                </span>
                            </div>
                            <LikeBtn className={styles.like} typeShow={"full"} />
                        </div>
                    )}
                        <div className={styles.textContent}>
                            <div className={styles.tags}>
                                <span>{tags || '#no-tags'}</span>
                            </div>
                            <span>{description || 'Без описания'}</span>
                        </div>
                </div>
            )}
            
            {edited && (
                <div className={styles.editContent}>
                    <div className={styles.form}>
                        <CustomTextArea
                            value={editTags}
                            onChange={handleTagsChange}
                            maxLength={MAX_LENGTH}
                            placeholder="например: #rec #fyp #wenclair.."
                            label="Тэги:"
                            id="editTags"
                        />

                        <CustomTextArea
                            value={editDescription}
                            onChange={handleDescriptionChange}
                            maxLength={MAX_LENGTH}
                            placeholder="Пара слов.."
                            label="Описание:"
                            id="editDescription"
                        />
                    </div>
                    <div className={styles.btnArea}>
                        <DefaultBtn text={'Сохранить'} />
                    </div>
                </div>
            )}

            {!edited && isOwner && (
                <Link to={`/art/${artId}/edit`} className={styles.edit}>
                    <img src={editIcon} alt="Редактировать" />
                    <span>Редактировать</span>
                </Link>
            )}

        </div>
    );
}