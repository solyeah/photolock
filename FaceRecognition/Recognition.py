import face_recognition

userInput = input("검사할 데이터를 고르세요 : ")
imagename = userInput.split()
image1 = imagename[0]
image2 = imagename[1]
# Load the jpg files into numpy arrays
saved_image = face_recognition.load_image_file("./images/" + image1 + ".jpg")
loaded_image = face_recognition.load_image_file("./images/" + image2 + ".jpg")

# Get the face encodings for each face in each image file
# Since there could be more than one face in each image, it returns a list of encodings.
# But since I know each image only has one face, I only care about the first encoding in each image, so I grab index 0.
try:
    saved_face_encoding = face_recognition.face_encodings(saved_image)[0]
    loaded_face_encoding = face_recognition.face_encodings(loaded_image)[0]
except IndexError:
    print("I wasn't able to locate any faces in at least one of the images. Check the image files. Aborting...")
    quit()

known_faces = [
    saved_face_encoding
]

# results is an array of True/False telling if the unknown face matched anyone in the known_faces array
results = face_recognition.compare_faces(known_faces, loaded_face_encoding)

print("동일인물이 맞습니까? {}".format(results[0]))