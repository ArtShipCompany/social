import { useState } from 'react';
import styles from './Switcher.module.css';

export default function Switcher({tabs, activeTab, onTabChange}) {
    return (
        <div className={styles.switcher}>
            {tabs.map((tab) => (
                <button
                    key={tab.id}
                    type="button"
                    className={`${styles.switcherTab} ${activeTab === tab.id ? styles.active : ''}`}
                    onClick={() => onTabChange(tab.id)}
                    role="tab"
                    aria-selected={activeTab === tab.id}
                >
                    {tab.label}
                </button>
            ))}
        </div>
    )
}