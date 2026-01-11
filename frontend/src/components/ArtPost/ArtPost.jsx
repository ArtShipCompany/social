import { Link } from 'react-router-dom';
import { useState } from 'react';
import styles from './ArtPost.module.css';
import LikeBtn from '../LikeBtn/LikeBtn';
import DefaultBtn from '../DefaultBtn/DefaultBtn';
import CustomTextArea from '../CustomTextArea/CustomTextArea';

import editIcon from '../../assets/edit-profile-icon.svg'

export default function ArtPost({ 
  edited = false, 
// этот флаг мок на то ты владелец арта, который открыл, или нет   
  isOwner = false,
  artId,  
  image,
// владелец арта   
  owner, 
  description = '', 
  tags = '', 
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
                    {/* вот тут как раз отображается владелец арта */}
                    {!isOwner && (
                        <div className={styles.textAndLike}>
                            <div className={styles.owner}>
                                <Link to={`/profile/${owner.id}`} className={styles.ownerLink}>
                                    <img src={owner.pfp} alt="" className={styles.pfp}/>
                                    <span className={styles.nickname}>@
                                    <span className={styles.link}>{owner.nickname}</span>
                                    </span>
                                </Link>
                            </div>
                            <LikeBtn className={styles.like} typeShow={"full"} amountLikes={1249} />
                        </div>
                    )}
                        <div className={styles.textContent}>
                            <div className={styles.tags}>
                                {/* тэги */}
                                <span>{tags || '#no-tags'}</span>
                            </div>
                                {/* описание */}
                                <span>{description || 'Без описания'}</span>
                        </div>
                </div>
            )}
            
            {/* этот компонент используется так же и для изменения поста (на стр. EditArt) */}
            {edited && (
                <div className={styles.editContent}>
                    <div className={styles.form}>
                        <CustomTextArea
                            value={editTags}
                            onChange={handleTagsChange}
                            maxLength={MAX_LENGTH}
                            placeholder="например: #rec #fyp.."
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