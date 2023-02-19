// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { getAnalytics } from "firebase/analytics";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
  apiKey: "AIzaSyDUrlI86GZPyia_lAmJqpxFrhra0uwCPgU",
  authDomain: "textbase-cd7c3.firebaseapp.com",
  projectId: "textbase-cd7c3",
  storageBucket: "textbase-cd7c3.appspot.com",
  messagingSenderId: "134549353988",
  appId: "1:134549353988:web:4d8c55f1d7c947a681a751",
  measurementId: "G-2J38DS96TK"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);