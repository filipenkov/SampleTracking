package com.atlassian.jira.servlet;

import com.atlassian.jira.ComponentManager;
import com.octo.captcha.CaptchaException;
import com.octo.captcha.service.CaptchaServiceException;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class JiraCaptchaServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(JiraCaptchaServlet.class);

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    {
        try
        {
            byte[] captchaChallengeAsJpeg;
            // the output stream to render the captcha image as jpeg into
            ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
            try
            {
                // get the session id that will identify the generated captcha.
                // the same id must be used to validate the response, the session id is a good candidate!
                String captchaId = httpServletRequest.getSession().getId();
                // call the ImageCaptchaService getChallenge method
                BufferedImage challenge = null;
                while(challenge == null)
                {
                    try
                    {
                        challenge = ComponentManager.getComponentInstanceOfType(JiraCaptchaService.class).getImageCaptchaService().getImageChallengeForID(captchaId, httpServletRequest.getLocale());
                    }
                    catch (CaptchaException e)
                    {
                        log.debug("CaptchaException thrown when image was being generated. This was most likely caused by running on OS X which has font size issues. Ignoring the exception. See http://jcaptcha.octo.com/jira/browse/FWK-58.", e);
                    }
                }

                // a jpeg encoder
                writeJpegImage(jpegOutputStream, challenge);
            }
            catch (IllegalArgumentException e)
            {
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            catch (CaptchaServiceException e)
            {
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            captchaChallengeAsJpeg = jpegOutputStream.toByteArray();

            // flush it in the response
            httpServletResponse.setHeader("Cache-Control", "no-store");
            httpServletResponse.setHeader("Pragma", "no-cache");
            httpServletResponse.setDateHeader("Expires", 0);
            httpServletResponse.setContentType("image/jpeg");
            ServletOutputStream responseOutputStream = httpServletResponse.getOutputStream();
            responseOutputStream.write(captchaChallengeAsJpeg);
            responseOutputStream.flush();
            responseOutputStream.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void writeJpegImage(final ByteArrayOutputStream os, final BufferedImage bufferedImage) throws IOException
    {
        final ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(os);
        final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        writer.setOutput(imageOutputStream);
        writer.write(bufferedImage);
    }
}