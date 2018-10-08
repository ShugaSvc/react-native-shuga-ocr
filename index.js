import {NativeModules} from 'react-native';

const {RNShugaOcr} = NativeModules;

const SugarOCR = {
  /**
   * @param {string} data - base64 image
   * @returns {Promise}
   */
  scanTextInImage: function (data) {
    return new Promise((resolve, reject) => {
      RNShugaOcr.scanTextInImage(data, (success) => {
        resolve(success);
      }, (error) => {
        reject(error);
      });
    });
  },
};

export default SugarOCR;
