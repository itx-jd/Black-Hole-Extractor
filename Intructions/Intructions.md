# Configuration Guide

## Firebase Setup

1. **Download and Add `google-services.json`**  
   - Place your `google-services.json` file in the following directory:  
     `app/google-services.json`

2. **Set up Firebase Realtime Database**  
   - In the Firebase console, set the following key-value pair:  
     `app_version: "1.0"`

3. **Firebase Rules**  
   - Set the Firebase rules to allow read write access. Ensure the rules are configured appropriately to permit the required data access.

4. **Troubleshooting**  
   - If you encounter any issues during the Firebase setup, please refer to the provided screenshots for guidance.

## API Setup

1. **Create RapidAPI Account**  
   - Register for an account on [RapidAPI](https://rapidapi.com).

2. **Subscribe to the Required APIs**  
   - Subscribe to the following APIs and retrieve the API keys:

   - [Social Download All in One API](https://rapidapi.com/nguyenmanhict-MuTUtGWD7K/api/social-download-all-in-one)
     - API key: `<string name="social_download_all_in_one_key">api_key</string>`

   - [YouTube Video and Shorts Downloader API](https://rapidapi.com/ugoBoy/api/youtube-video-and-shorts-downloader1)
     - API key: `<string name="youtube_video_and_shorts_downloader">api_key</string>`

3. **Insert API Keys into Your Project**  
   - Use the respective API keys in your project by placing them in the designated fields as shown above.

## Support

If you require further assistance, feel free to contact me at:  
**Email:** [jawad2k01@gmail.com](mailto:jawad2k01@gmail.com)
