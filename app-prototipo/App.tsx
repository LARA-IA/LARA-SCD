import React, { useState } from 'react';
import { StatusBar } from 'expo-status-bar';
import { Button, Image, StyleSheet, Text, View } from 'react-native';
import { LoadedImage } from './components/LoadedImage';
import * as ImagePicker from "expo-image-picker";
import * as ImageManipulator from "expo-image-manipulator";
import axios from 'axios';


export default function App() {
  const [imgURI,setImg] = useState(null)
  const imgOptions = {mediaTypes : ImagePicker.MediaTypeOptions.Images}
  const url = "http://localhost:8080/predict";

  const headers =  {
    'accept': 'application/json',
    "Content-Type": 'multipart/form-data'
  }

  const imgConfiguer = async (result) =>{
    setImg(result.assets[0].uri)
    const manipResult = await ImageManipulator.manipulateAsync(
      String(imgURI),
      [{ resize: { width: 100, height: 75 } }]); 
    setImg(manipResult.uri);    
  }


  const launchLibrary = async () =>{
    const result = await ImagePicker.launchImageLibraryAsync(imgOptions);

    if(!result.canceled){
      imgConfiguer(result);
    }

  };

  const launchCamera = async () => {
    const result = await ImagePicker.launchCameraAsync(imgOptions);

    if(!result.canceled){
      imgConfiguer(result);
    }
  }

  const consumeAPI = async ()=>{
    if(imgURI != null){
      let data = new FormData();
      data.append('imagem', imgURI, "imagem");


      axios
      .post(url,data,{headers})
      .then(response =>{
        console.log(response.data);
      })
      .catch((error) =>{
        console.log(error)
      })

    }
  }


  return (
    <View style={styles.container}>
      <Button title="Escolher imagem da galeria" onPress={launchLibrary}></Button>
      <Button title="Tirar foto" onPress={launchCamera}></Button>
      <Image source={imgURI}/>
      <Button title="Enviar" onPress={consumeAPI}></Button>
      <StatusBar style="auto" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
