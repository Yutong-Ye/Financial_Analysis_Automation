// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { getAnalytics } from "firebase/analytics";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries
import {getFirestore} from "firebase/firestore";

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
  apiKey: "AIzaSyAqOaVi8iMaWSQiru3KF_6isXmUOufUxlw",
  authDomain: "application-tracker-dd463.firebaseapp.com",
  projectId: "application-tracker-dd463",
  storageBucket: "application-tracker-dd463.appspot.com",
  messagingSenderId: "1073626618789",
  appId: "1:1073626618789:web:d644c01b9cfd6fbc90e64c",
  measurementId: "G-6JKM54KFVR"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);

export const db = getFirestore(app)
