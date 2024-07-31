import { Image, StyleSheet, Text, View } from 'react-native';


export function LoadedImage(props){
    return (
        <Image source={props.imgURI}/>
    );
  }