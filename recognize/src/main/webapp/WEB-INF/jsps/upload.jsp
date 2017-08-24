<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Upload Files</title>
</head>
<body>
    <div>

        <h3>Choose File to Upload to Server</h3>

        <form action="upload3" method="post" enctype="multipart/form-data">

            <input type="file" accept="audio/*" name="file" multiple="multiple" />
            <br />
            <input type="submit" value="Upload" />

        </form>

    </div>

</body>
</html>